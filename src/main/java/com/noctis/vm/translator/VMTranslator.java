package com.noctis.vm.translator;

import com.noctis.vm.translator.common.InstructionType;
import com.noctis.vm.translator.exception.AssemblyTranslationException;
import com.noctis.vm.translator.exception.InstructionParseException;
import com.noctis.vm.translator.parser.VMInstructionParser;
import com.noctis.vm.translator.writer.AssemblyCodeWriter;

import java.io.IOException;

/**
 * @author Noctis
 * @date 2025/02/27
 */
public class VMTranslator {
   public static void main(String[] args) throws InstructionParseException, IOException, AssemblyTranslationException {
      if (args.length != 1) {
         System.out.println("Usage: java -jar HackVMTranslator-1.0.jar 'absolute / relative file name'");
         System.exit(1);
      }
      String fileName = args[0];
      VMInstructionParser instructionParser = new VMInstructionParser(fileName);
      AssemblyCodeWriter assemblyCodeWriter = new AssemblyCodeWriter(fileName);
      while (instructionParser.hasMoreLines()){
         instructionParser.advance();
         InstructionType instructionType = instructionParser.commandType();
         if (InstructionType.C_ARITHMETIC.equals(instructionType)) {
            assemblyCodeWriter.writeArithmetic(instructionParser.arg1());
         } else {
            assemblyCodeWriter.writePushPop(instructionType, instructionParser.arg1(), instructionParser.arg2());
         }
      }
      assemblyCodeWriter.close();
   }
}
