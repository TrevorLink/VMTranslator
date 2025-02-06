package com.noctis.vm.translator.writer;

import com.noctis.vm.translator.common.InstructionType;
import com.noctis.vm.translator.common.VMConstants;
import com.noctis.vm.translator.util.StringUtils;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Noctis
 * @date 2025/02/02
 */
public class AssemblyCodeWriter {

   private final FileWriter fw;

   public AssemblyCodeWriter(String fileName) {
      if (StringUtils.isEmpty(fileName)) {
         throw new RuntimeException("Empty result file name");
      }
      fileName = fileName + VMConstants.RESULT_ASM_FILE_SUFFIX;
      try {
         fw = new FileWriter(fileName);
      } catch (IOException e) {
         throw new RuntimeException("Error occurred when trying to write file :" + fileName, e);
      }
   }

   public void writeArithmetic(String instruction) throws IOException {
      String content = translateArithmeticCommandToAssembly(instruction);
      if (fw != null && StringUtils.isNotEmpty(content)) {
         fw.write(content);
      }
   }

   public void writePushPop(InstructionType instructionType, String segment, Integer index) throws IOException {
      String content;
      switch (instructionType) {
         case C_PUSH:
            content = translatePushCommandToAssembly(segment, index);
            break;
         case C_POP:
            content = translatePopCommandToAssembly(segment, index);
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

   private String translatePushCommandToAssembly(String segment, Integer index) {
      return "";
   }

   private String translatePopCommandToAssembly(String segment, Integer index) {
      return "";
   }

   private String translateArithmeticCommandToAssembly(String command) {
      return "";
   }
}
