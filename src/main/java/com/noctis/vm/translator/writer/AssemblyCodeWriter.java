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
   private final String fileName;

   public AssemblyCodeWriter(String fileName) {
      if (StringUtils.isEmpty(fileName)) {
         throw new RuntimeException("Empty result file name");
      }
      this.fileName = fileName;
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

   private String translatePushCommandToAssembly(String segment, Integer index, String fileName) {
      //constant
      String constant = "@i\nD=A\n@SP\nA=M\nM=D\n";
      //local,argument,this,that
      String localAndSoOn = "D=M\n@i\nD=D+A\nA=D\nD=M\n@SP\nA=M\nM=D\n";
      //static
      String statics = "@" + fileName + "." + index + "\n";
      //temp
      String tmp = "@5\nD=A\n@i\nD=D+A\n@SP\nA=M\nM=D\n";
      //pointer push pointer 0
      String ptr = "@3\nD=A\n@i\nA=D+A\nA=M\nD=M\n@SP\nA=M\nM=D\n";
      String spIncrement = "@SP\nM=M+1\n";
      return "";
   }

   private String translatePopCommandToAssembly(String segment, Integer index, String fileName) {
      return "";
   }

   private String translateArithmeticCommandToAssembly(String command) {
      return "";
   }
}
