package com.noctis.vm.translator.parser;

import com.noctis.vm.translator.common.InstructionType;
import com.noctis.vm.translator.common.VMConstants;
import com.noctis.vm.translator.exception.InstructionParseException;
import com.noctis.vm.translator.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Noctis
 * @date 2025/02/02
 */
public class VMInstructionParser {

   private final List<String> vmInstructionList;

   private String currentInstruction;

   private int index;

   /**
    * Open the vm file and initialize the parser by reading the vm instructions
    *
    * @param vmFileLocation absolute file path for the vm file
    */
   public VMInstructionParser(String vmFileLocation) throws InstructionParseException {
      index = 0;
      currentInstruction = null;
      try {
         vmInstructionList = Files.lines(Paths.get(vmFileLocation))
                 .filter(vmInstruction -> !isCommentLine(vmInstruction))
                 //ignore all the whitespace at the start and end of the instruction
                 .map(String::trim)
                 .collect(Collectors.toList());
      } catch (IOException e) {
         throw new RuntimeException("Error occurred when trying to read the vm file:" + vmFileLocation, e);
      }
      //Validate all the non-whitespace instructions in vm file
      for (String instruction : vmInstructionList) {
         validate(instruction);
      }
   }

   /**
    * Checks if there is more work to do
    *
    * @return If the parser already reach the end of the vm file
    */
   public boolean hasMoreLines() {
      return index < vmInstructionList.size();
   }

   /**
    * Reads the next command and makes it the current command
    */
   public void advance() {
      if (!hasMoreLines()) {
         return;
      }
      currentInstruction = vmInstructionList.get(index++);
   }

   /**
    * Returns a constant representing the type of current vm instruction
    *
    * @return Type of VM instruction
    */
   public InstructionType commandType() {
      if (StringUtils.isEmpty(currentInstruction)) {
         return null;
      }
      if (VMConstants.ARITHMETIC_COMMAND_SET.contains(currentInstruction)) {
         return InstructionType.C_ARITHMETIC;
      } else if (currentInstruction.startsWith(VMConstants.PUSH_COMMAND_PREFIX)) {
         return InstructionType.C_PUSH;
      } else if (currentInstruction.startsWith(VMConstants.POP_COMMAND_PREFIX)) {
         return InstructionType.C_POP;
      } else {
         return null;
      }
   }

   /**
    * @return The first argument of the vm instruction
    */
   public String arg1() {
      InstructionType instructionType = this.commandType();
      if (instructionType == null) {
         return null;
      }
      //For arithmetic instruction return the instruction itself
      if (InstructionType.C_ARITHMETIC.equals(instructionType)) {
         return currentInstruction;
      }
      //C_PUSH/C_POP returns the virtual segment identifier, an valid c_push/c_pop vm instruction only contains two whitespace
      return currentInstruction.substring(currentInstruction.indexOf(" ") + 1, currentInstruction.lastIndexOf(" "));
   }

   /**
    * @return the second argument of the current command
    */
   public Integer arg2() {
      InstructionType instructionType = this.commandType();
      //Should be called only if the instruction type is push/pop type
      if (instructionType == null || InstructionType.C_ARITHMETIC.equals(instructionType)) {
         return null;
      }
      return Integer.parseInt(currentInstruction.substring(currentInstruction.lastIndexOf(" ") + 1));
   }

   /**
    * Check whether the instruction is an VM instruction comment
    *
    * @param vmInstruction instruction
    * @return whether is comment
    */
   private boolean isCommentLine(String vmInstruction) {
      if (StringUtils.isEmpty(vmInstruction)) {
         return true;
      }
      return vmInstruction.contains(VMConstants.COMMENT_IDENTIFIER);
   }

   /**
    * Check whether the instruction is valid by regex
    *
    * @param instruction instruction that need to validate
    * @throws InstructionParseException When instruction does not match the regular expression (for those who contains more than 2 white-space will not match also)
    */
   private void validate(String instruction) throws InstructionParseException {
      if (StringUtils.isEmpty(instruction)) {
         throw new InstructionParseException("Instruction is empty");
      }
      Pattern pattern = Pattern.compile(VMConstants.INSTRUCTION_PATTERN);
      Matcher matcher = pattern.matcher(instruction);
      if (!VMConstants.ARITHMETIC_COMMAND_SET.contains(instruction) && !matcher.matches()) {
         throw new InstructionParseException("Invalid instruction: " + instruction);
      }
   }
}
