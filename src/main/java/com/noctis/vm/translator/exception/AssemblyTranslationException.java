package com.noctis.vm.translator.exception;

/**
 * @author Noctis
 * @date 2025/02/07
 */
public class AssemblyTranslationException extends Exception {

   public AssemblyTranslationException(String message) {
      super(message);
   }

   public AssemblyTranslationException(String message, Throwable cause) {
      super(message, cause);
   }
}
