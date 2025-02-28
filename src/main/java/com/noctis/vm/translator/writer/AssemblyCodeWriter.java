package com.noctis.vm.translator.writer;

import com.noctis.vm.translator.common.InstructionType;
import com.noctis.vm.translator.common.VMConstants;
import com.noctis.vm.translator.exception.AssemblyTranslationException;
import com.noctis.vm.translator.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

/**
 * @author Noctis
 * @date 2025/02/02
 */
public class AssemblyCodeWriter {

   private final FileWriter fw;
   private final String fileName;
   private static final String newLine = System.lineSeparator();
   private int labelIndexSuffix = 0;

   public AssemblyCodeWriter(String fileName) throws IOException {
      String filePrefix = "";
      //Absolute path file name cutting
      if (fileName.contains(File.separator)) {
         filePrefix = fileName.substring(0, fileName.lastIndexOf(File.separator) + 1);
         fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
      }
      this.fileName = fileName.substring(0, fileName.lastIndexOf("."));
      fileName = this.fileName + VMConstants.RESULT_ASM_FILE_SUFFIX;
      fw = new FileWriter(filePrefix + fileName);
   }

   public void writeArithmetic(String instruction) throws IOException {
      String content = translateArithmeticCommandToAssembly(instruction);
      write(content);
   }

   public void writePushPop(InstructionType instructionType, String segment, Integer index) throws AssemblyTranslationException, IOException {
      String content;
      switch (instructionType) {
         case C_PUSH:
            content = translatePushCommandToAssembly(segment, index, fileName);
            break;
         case C_POP:
            content = translatePopCommandToAssembly(segment, index, fileName);
            break;
         case C_ARITHMETIC:
         default:
            return;
      }
      write(content);
   }

   private void write(String content) throws IOException {
      if (StringUtils.isNotEmpty(content)) {
         fw.write(content);
      }
   }

   public void close() throws IOException {
      fw.close();
   }

   /**
    * Translate the push vm instructions to equivalent hack assembly code.
    * Notice that when executing the instruction set on vm emulator,
    * the push instruction only read the virtual segment (when the segment value is not {@code constant}),
    * the equivalent assembly code only write the corresponding value to RAM[SP]
    *
    * @param segment  push instruction segment, must be {@code constant/local/argument/this/that/static/temp/pointer}
    * @param index    push instruction operands
    * @param fileName vm filename, to generate static symbol Xxx.i
    * @return equivalent hack assembly code with separate lines
    */
   private String translatePushCommandToAssembly(String segment, Integer index, String fileName) throws AssemblyTranslationException {
      String setSegmentValueToD = null;
      String setRAMSpFromDAndSpIncrement = String.join(newLine, Arrays.asList(
              "@SP",
              "A=M",
              "M=D",
              "@SP",
              "M=M+1"
      )) + newLine;

      switch (segment) {
         case VMConstants.VIRTUAL_SEGMENT_CONSTANT:
            setSegmentValueToD = String.join(newLine, Arrays.asList(
                    "@" + index,
                    "D=A"
            )) + newLine;
            break;
         case VMConstants.VIRTUAL_SEGMENT_LOCAL:
         case VMConstants.VIRTUAL_SEGMENT_ARGUMENT:
         case VMConstants.VIRTUAL_SEGMENT_THIS:
         case VMConstants.VIRTUAL_SEGMENT_THAT:
            String symbol = VMConstants.VIRTUAL_SEGMENT_AND_SYMBOL_MAP.get(segment);
            if (StringUtils.isEmpty(symbol)) {
               throw new AssemblyTranslationException("Empty virtual-segment-symbol mapping for segment:" + segment);
            }
            setSegmentValueToD = String.join(newLine, Arrays.asList(
                    symbol,
                    "D=M",
                    "@" + index,
                    "A=D+A",
                    "D=M"
            )) + newLine;
            break;
         case VMConstants.VIRTUAL_SEGMENT_STATIC:
            setSegmentValueToD = this.getStaticInstructionAsmSymbol(fileName, index) + "D=M" + newLine;
            break;
         case VMConstants.VIRTUAL_SEGMENT_TEMP:
            setSegmentValueToD = String.join(newLine, Arrays.asList(
                    "@5",
                    "D=A",
                    "@" + index,
                    "A=D+A",
                    "D=M"
            )) + newLine;
            break;
         case VMConstants.VIRTUAL_SEGMENT_POINTER:
            setSegmentValueToD = String.join(newLine, Arrays.asList(
                    "@3",
                    "D=A",
                    "@" + index,
                    "A=D+A",
                    "D=M"
            )) + newLine;
            break;
      }
      return setSegmentValueToD + setRAMSpFromDAndSpIncrement;
   }

   /**
    * Translate the pop vm instructions to equivalent hack assembly code.
    * Notice that when executing the instruction set on vm emulator,
    * the pop instruction only write the virtual segment,
    * the equivalent assembly code will write the RAM[segment] to specific value
    *
    * @param segment  push instruction segment, must be {@code local/argument/this/that/static/temp/pointer},
    *                 note that {@code pop constant i} is not supported in hack virtual machine instruction
    * @param index    push instruction operands
    * @param fileName vm filename, to generate static symbol Xxx.i
    * @return equivalent hack assembly code with separate lines
    */
   private String translatePopCommandToAssembly(String segment, Integer index, String fileName) throws AssemblyTranslationException {
      String spDecrementAndSetRAMValueToD = String.join(newLine, Arrays.asList(
              "@SP",
              "AM=M-1",
              "D=M"
      )) + newLine;

      //calculate address according to different segment,then set RAM[address] = D
      String getDestinationAddressAndSetRAMValueFromD = null;
      switch (segment) {
         case VMConstants.VIRTUAL_SEGMENT_LOCAL:
         case VMConstants.VIRTUAL_SEGMENT_ARGUMENT:
         case VMConstants.VIRTUAL_SEGMENT_THIS:
         case VMConstants.VIRTUAL_SEGMENT_THAT:
            String symbol = VMConstants.VIRTUAL_SEGMENT_AND_SYMBOL_MAP.get(segment);
            if (StringUtils.isEmpty(symbol)) {
               throw new AssemblyTranslationException("Empty virtual-segment-symbol mapping for segment:" + segment);
            }
            getDestinationAddressAndSetRAMValueFromD = String.join(newLine, Arrays.asList(
                    "@R15",
                    "M=D",
                    symbol,
                    "D=M",
                    "@" + index,
                    "D=D+A",
                    "@R16",
                    "M=D",
                    "@R15",
                    "D=M",
                    "@R16",
                    "A=M",
                    "M=D"
            )) + newLine;
            break;
         case VMConstants.VIRTUAL_SEGMENT_STATIC:
            getDestinationAddressAndSetRAMValueFromD = this.getStaticInstructionAsmSymbol(fileName, index) + "M=D" + newLine;
            break;
         case VMConstants.VIRTUAL_SEGMENT_TEMP:
            int finalIndex = 5 + index;
            getDestinationAddressAndSetRAMValueFromD = String.join(newLine, Arrays.asList(
                    "@" + finalIndex,
                    "M=D"
            )) + newLine;
            break;
         case VMConstants.VIRTUAL_SEGMENT_POINTER:
            String base = index == 0 ? VMConstants.VIRTUAL_SEGMENT_AND_SYMBOL_MAP.get(VMConstants.VIRTUAL_SEGMENT_THIS)
                    : VMConstants.VIRTUAL_SEGMENT_AND_SYMBOL_MAP.get(VMConstants.VIRTUAL_SEGMENT_THAT);
            getDestinationAddressAndSetRAMValueFromD = String.join(newLine, Arrays.asList(
                    base,
                    "M=D"
            )) + newLine;
            break;
      }
      return spDecrementAndSetRAMValueToD + getDestinationAddressAndSetRAMValueFromD;
   }

   private String translateArithmeticCommandToAssembly(String command) {
      String spIncrement = "@SP" + newLine + "M=M+1";
      String saveSecondOperandToD = String.join(newLine, Arrays.asList(
              "@SP",
              "AM=M-1",
              "D=M"
      ));
      String loadFirstOperandToM = String.join(newLine, Arrays.asList(
              "@SP",
              "AM=M-1"
      ));

      String loadOperands = saveSecondOperandToD + newLine + loadFirstOperandToM;

      String computeResultAndPush = null;

      switch (command) {
         case VMConstants.ARITHMETIC_ADD:
            computeResultAndPush = "M=D+M";
            break;
         case VMConstants.ARITHMETIC_SUB:
            computeResultAndPush = "M=M-D";
            break;
         case VMConstants.ARITHMETIC_OR:
            computeResultAndPush = "M=D|M";
            break;
         case VMConstants.ARITHMETIC_AND:
            computeResultAndPush = "M=D&M";
            break;
         case VMConstants.ARITHMETIC_NEG:
            loadOperands = saveSecondOperandToD;
            computeResultAndPush = "M=-D";
            break;
         case VMConstants.ARITHMETIC_NOT:
            loadOperands = saveSecondOperandToD;
            computeResultAndPush = "M=!D";
            break;
         //Using jump to handle the eq/gt/lt commands in assembly
         case VMConstants.ARITHMETIC_EQ:
         case VMConstants.ARITHMETIC_GT:
         case VMConstants.ARITHMETIC_LT:
            //Using index that increment itself to fix multiple arithmetic call in same vm file
            String trueLabel = "TRUE" + labelIndexSuffix;
            String continueLabel = "CONTINUE" + labelIndexSuffix;
            labelIndexSuffix++;
            computeResultAndPush = String.join(newLine, Arrays.asList(
                    "D=M-D",
                    "@" + trueLabel,
                    "D;J" + command.toUpperCase(Locale.ROOT),
                    //FALSE
                    "@SP",
                    "A=M",
                    "M=0",
                    "@" + continueLabel,
                    "0;JMP",
                    //TRUE
                    "(" + trueLabel + ")",
                    "@SP",
                    "A=M",
                    "M=-1",
                    "(" + continueLabel + ")"
            ));
            break;
      }
      return loadOperands + newLine + computeResultAndPush + newLine + spIncrement + newLine;
   }

   private String getStaticInstructionAsmSymbol(String fileName, Integer index) {
      return "@" + fileName + "." + index + newLine;
   }
}