package com.noctis.vm.translator.exception;

/**
 * @author Noctis
 * @date 2025/02/06
 */
public class InstructionParseException extends Exception{

   public InstructionParseException(String message){
      super(message);
   }

   public InstructionParseException(String message, Throwable cause){
      super(message, cause);
   }
}
