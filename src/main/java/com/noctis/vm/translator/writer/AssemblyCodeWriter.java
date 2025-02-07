package com.noctis.vm.translator.writer;

import com.noctis.vm.translator.common.InstructionType;
import com.noctis.vm.translator.common.VMConstants;
import com.noctis.vm.translator.exception.AssemblyTranslationException;
import com.noctis.vm.translator.util.StringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author Noctis
 * @date 2025/02/02
 */
public class AssemblyCodeWriter {

   private final FileWriter fw;
   private final String fileName;
   private static final String newLine = System.lineSeparator();

   public AssemblyCodeWriter(String fileName) throws AssemblyTranslationException {
      if (StringUtils.isEmpty(fileName)) {
         throw new RuntimeException("Empty result file name");
      }
      this.fileName = fileName;
      fileName = fileName + VMConstants.RESULT_ASM_FILE_SUFFIX;
      try {
         fw = new FileWriter(fileName);
      } catch (IOException e) {
         throw new AssemblyTranslationException("Error occurred when trying to write file :" + fileName, e);
      }
   }

   public void writeArithmetic(String instruction) throws IOException {
      String content = translateArithmeticCommandToAssembly(instruction);
      if (fw != null && StringUtils.isNotEmpty(content)) {
         fw.write(content);
      }
   }

   public void writePushPop(InstructionType instructionType, String segment, Integer index) throws IOException, AssemblyTranslationException {
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
      if (fw != null && StringUtils.isNotEmpty(content)) {
         fw.write(content);
      }
   }

   public void close() throws IOException {
      if (fw != null) {
         fw.close();
      }
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
      String spIncrementAndSetRAMSpFromD = String.join(newLine, Arrays.asList(
              "@SP",
              "M=M+1",
              "@SP",
              "A=M",
              "M=D"
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
      return setSegmentValueToD + spIncrementAndSetRAMSpFromD;
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
              "M=M-1",
              "A=M",
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
                    symbol,
                    "D=M",
                    "@" + index,
                    "A=D+A",
                    "M=D"
            )) + newLine;
            break;
         case VMConstants.VIRTUAL_SEGMENT_STATIC:
            getDestinationAddressAndSetRAMValueFromD = this.getStaticInstructionAsmSymbol(fileName, index) + "M=D" + newLine;
            break;
         case VMConstants.VIRTUAL_SEGMENT_TEMP:
            getDestinationAddressAndSetRAMValueFromD = String.join(newLine, Arrays.asList(
                    "@5",
                    "D=A",
                    "@" + index,
                    "A=D+A",
                    "M=D"
            )) + newLine;
            break;
         case VMConstants.VIRTUAL_SEGMENT_POINTER:
            getDestinationAddressAndSetRAMValueFromD = String.join(newLine, Arrays.asList(
                    "@3",
                    "D=A",
                    "@" + index,
                    "A=D+A",
                    "M=D"
            )) + newLine;
            break;
      }
      return spDecrementAndSetRAMValueToD + getDestinationAddressAndSetRAMValueFromD;
   }

   private String translateArithmeticCommandToAssembly(String command) {
      List<String> saveSecondOperandToDAsmList = Arrays.asList(
              "@SP",
              "AM=M-1",
              "D=M"
      );
      List<String> getFirstOperandAsmList = Arrays.asList(
              "@SP",
              "AM=M-1"
      );
      String saveSecondOperandToD = String.join(newLine, saveSecondOperandToDAsmList);

      String loadOperands = saveSecondOperandToD + newLine + String.join(newLine, getFirstOperandAsmList);

      String computeAndPushResult = null;

      switch (command) {
         case VMConstants.ARITHMETIC_ADD:
            computeAndPushResult = "M=M+D";
            break;
         case VMConstants.ARITHMETIC_SUB:
            computeAndPushResult = "M=M-D";
            break;
         case VMConstants.ARITHMETIC_OR:
            computeAndPushResult = "M=M|D";
            break;
         case VMConstants.ARITHMETIC_AND:
            computeAndPushResult = "M=M&D";
            break;
         case VMConstants.ARITHMETIC_NEG:
            loadOperands = saveSecondOperandToD;
            computeAndPushResult = "M=-D";
            break;
         case VMConstants.ARITHMETIC_NOT:
            loadOperands = saveSecondOperandToD;
            computeAndPushResult = "M=!D";
            break;
         case VMConstants.ARITHMETIC_EQ:
         case VMConstants.ARITHMETIC_GT:
         case VMConstants.ARITHMETIC_LT:
            computeAndPushResult = String.join(newLine, Arrays.asList(
                    "@SP",
                    "AM=M-1",
                    "D=M",
                    "@SP",
                    "AM=M-1",
                    "D=M-D",
                    "@TRUE",
                    "D;J" + command.toUpperCase(Locale.ROOT),
                    //FALSE
                    "@SP",
                    "A=M",
                    "M=0",
                    "@CONTINUE",
                    "0;JMP",
                    //TRUE
                    "(TRUE)",
                    "@SP",
                    "A=M",
                    "M=-1",
                    "(CONTINUE)"
            ));
            break;
      }
      return loadOperands + newLine + computeAndPushResult + newLine;
   }

   private String getStaticInstructionAsmSymbol(String fileName, Integer index) {
      return "@" + fileName + "." + index + newLine;
   }
}