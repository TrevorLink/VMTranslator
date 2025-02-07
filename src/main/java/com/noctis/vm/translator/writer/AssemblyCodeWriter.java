package com.noctis.vm.translator.writer;

import com.noctis.vm.translator.common.InstructionType;
import com.noctis.vm.translator.common.VMConstants;
import com.noctis.vm.translator.exception.AssemblyTranslationException;
import com.noctis.vm.translator.util.StringUtils;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Noctis
 * @date 2025/02/02
 */
public class AssemblyCodeWriter {

   private final FileWriter fw;
   private final String fileName;

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
      //SP++ RAM[SP] = D
      String spIncrementAndSetRAMSpFromD = "@SP\nM=M+1\n@SP\nA=M\nM=D\n";
      switch (segment) {
         case VMConstants.VIRTUAL_SEGMENT_CONSTANT:
            //RAM[SP]=i
            setSegmentValueToD = "@" + index + "\nD=A\n";
            break;
         case VMConstants.VIRTUAL_SEGMENT_LOCAL:
         case VMConstants.VIRTUAL_SEGMENT_ARGUMENT:
         case VMConstants.VIRTUAL_SEGMENT_THIS:
         case VMConstants.VIRTUAL_SEGMENT_THAT:
            //RAM[SP]=RAM[SYMBOL+i]
            String symbol = VMConstants.VIRTUAL_SEGMENT_AND_SYMBOL_MAP.get(segment);
            if (StringUtils.isEmpty(symbol)) {
               throw new AssemblyTranslationException("Empty virtual-segment-symbol mapping for segment:" + segment);
            }
            setSegmentValueToD = symbol + "D=M\n" + "@" + index + "\nA=D+A\nD=M\n";
            break;
         case VMConstants.VIRTUAL_SEGMENT_STATIC:
            setSegmentValueToD = this.getStaticInstructionAsmSymbol(fileName, index) + "D=M";
            break;
         case VMConstants.VIRTUAL_SEGMENT_TEMP:
            //RAM[SP]=RAM[5+i]
            setSegmentValueToD = "@5\nD=A\n" + "@" + index + "\nA=D+A\nD=M\n";
            break;
         case VMConstants.VIRTUAL_SEGMENT_POINTER:
            //RAM[SP]=RAM[THIS]/RAM[THAT]
            setSegmentValueToD = "@3\nD=A\n" + "@" + index + "\nA=D+A\nD=M\n";
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
      //SP-- D=RAM[SP]
      String spDecrementAndSetRAMValueToD = "@SP\nM=M-1\nA=M\nD=M\n";
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
            getDestinationAddressAndSetRAMValueFromD = symbol + "D=M\n" + "@" + index + "\nA=D+A\nM=D\n";
            break;
         case VMConstants.VIRTUAL_SEGMENT_STATIC:
            getDestinationAddressAndSetRAMValueFromD = this.getStaticInstructionAsmSymbol(fileName, index) + "M=D";
            break;
         case VMConstants.VIRTUAL_SEGMENT_TEMP:
            getDestinationAddressAndSetRAMValueFromD = "@5\nD=A\n" + "@" + index + "\nA=D+A\nM=D\n";
            break;
         case VMConstants.VIRTUAL_SEGMENT_POINTER:
            getDestinationAddressAndSetRAMValueFromD = "@3\nD=A\n" + "@" + index + "\nA=D+A\nM=D\n";
            break;
      }
      return spDecrementAndSetRAMValueToD + getDestinationAddressAndSetRAMValueFromD;
   }

   private String translateArithmeticCommandToAssembly(String command) {
      return "";
   }

   private String getStaticInstructionAsmSymbol(String fileName, Integer index) {
      return "@" + fileName + "." + index + "\n";
   }
}
