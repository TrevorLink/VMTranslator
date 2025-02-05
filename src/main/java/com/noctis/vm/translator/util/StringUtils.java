package com.noctis.vm.translator.util;

/**
 * @author Noctis
 * @date 2025/02/02
 */
public class StringUtils {

   public static boolean isNotEmpty(String str) {
      return str != null && !str.isEmpty();
   }

   public static boolean isEmpty(String str) {
      return !isNotEmpty(str);
   }
}
