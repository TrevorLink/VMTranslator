package com.noctis.vm.translator.common;

import java.util.*;

/**
 * @author Noctis
 * @date 2025/02/02
 */
public class VMConstants {

   public static final String INSTRUCTION_PATTERN = "^(push|pop)\\s(constant|local|argument|this|that|static|temp|pointer)\\s\\d+$";

   public static final Set<String> ARITHMETIC_COMMAND_SET = new HashSet<>(Arrays.asList("add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"));

   //----- VM Instruction Prefix -----//
   public static final String INSTRUCTION_PREFIX_PUSH = "push";

   public static final String INSTRUCTION_PREFIX_POP = "pop";

   //----- Virtual Memory Segment Name -----//
   public static final String VIRTUAL_SEGMENT_CONSTANT = "constant";

   public static final String VIRTUAL_SEGMENT_LOCAL = "local";

   public static final String VIRTUAL_SEGMENT_ARGUMENT = "argument";

   public static final String VIRTUAL_SEGMENT_THIS = "this";

   public static final String VIRTUAL_SEGMENT_THAT = "that";

   public static final String VIRTUAL_SEGMENT_TEMP = "temp";

   public static final String VIRTUAL_SEGMENT_STATIC = "static";

   public static final String VIRTUAL_SEGMENT_POINTER = "pointer";

   //----- Misc -----//
   public static final String COMMENT_IDENTIFIER = "//";

   public static final String RESULT_ASM_FILE_SUFFIX = ".asm";

   public static final Map<String, String> VIRTUAL_SEGMENT_AND_SYMBOL_MAP = new HashMap<>(4);

   static {
      VIRTUAL_SEGMENT_AND_SYMBOL_MAP.put(VIRTUAL_SEGMENT_LOCAL, "@LCL\n");
      VIRTUAL_SEGMENT_AND_SYMBOL_MAP.put(VIRTUAL_SEGMENT_ARGUMENT, "@ARG\n");
      VIRTUAL_SEGMENT_AND_SYMBOL_MAP.put(VIRTUAL_SEGMENT_THIS, "@THIS\n");
      VIRTUAL_SEGMENT_AND_SYMBOL_MAP.put(VIRTUAL_SEGMENT_THAT, "@THAT\n");
   }
}
