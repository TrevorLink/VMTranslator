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
    * Translate the push vm instructions to equivalent hack assembly code
    *
    * @param segment  push instruction segment, must be {@code constant/local/argument/this/that/static/temp/pointer}
    * @param index    push instruction operands
    * @param fileName vm filename, to generate static symbol Xxx.i
    * @return equivalent hack assembly code with separate lines
    */
   private String translatePushCommandToAssembly(String segment, Integer index, String fileName) throws AssemblyTranslationException {
      String mainAsmCode = null;
      //RAM[SP] = D
      String setRAMSpToD = "@SP\nA=M\nM=D\n";
      //SP++
      String spIncrement = "@SP\nM=M+1\n";
      switch (segment) {
         case VMConstants.VIRTUAL_SEGMENT_CONSTANT:
            mainAsmCode = "@i\nD=A\n@SP\nA=M\nM=D\n";
            break;
         case VMConstants.VIRTUAL_SEGMENT_LOCAL:
         case VMConstants.VIRTUAL_SEGMENT_ARGUMENT:
         case VMConstants.VIRTUAL_SEGMENT_THIS:
         case VMConstants.VIRTUAL_SEGMENT_THAT:
            String identifier = VMConstants.virtualSegmentAndIdentifierMap.get(segment);
            if (StringUtils.isEmpty(identifier)) {
               throw new AssemblyTranslationException("Empty virtual-segment-identifier mapping for segment:" + segment);
            }
            mainAsmCode = identifier + "D=M\n@i\nA=D+A\nD=M\n" + setRAMSpToD;
            break;
         case VMConstants.VIRTUAL_SEGMENT_STATIC:
            return "@" + fileName + "." + index + "\n";
         case VMConstants.VIRTUAL_SEGMENT_TEMP:
            mainAsmCode = "@5\nD=A\n@i\nA=D+A\nD=M\n" + setRAMSpToD;
            break;
         case VMConstants.VIRTUAL_SEGMENT_POINTER:
            mainAsmCode = "@3\nD=A\n@i\nA=D+A\nD=M\n" + setRAMSpToD;
            break;
      }
      return mainAsmCode + spIncrement;
   }

   private String translatePopCommandToAssembly(String segment, Integer index, String fileName) {
      return "";
   }

   private String translateArithmeticCommandToAssembly(String command) {
      return "";
   }
}
