package com.noctis.vm.translator.common;

import java.util.*;

/**
 * @author Noctis
 * @date 2025/02/02
 */
public class VMConstants {

   public static final String INSTRUCTION_PATTERN = "^(push|pop)\\s(constant|local|argument|this|that|static|temp|pointer)\\s\\d+$";

   public static final Set<String> ARITHMETIC_COMMAND_SET = new HashSet<>(Arrays.asList("add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"));

   public static final String PUSH_COMMAND_PREFIX = "push";

   public static final String POP_COMMAND_PREFIX = "pop";

   public static final String COMMENT_IDENTIFIER = "//";

   public static final String RESULT_ASM_FILE_SUFFIX = ".asm";

   public static final Map<String, String> virtualSegmentAndIdentifierMap = new HashMap<>(4);

   static {
      virtualSegmentAndIdentifierMap.put("local", "@LCL");
      virtualSegmentAndIdentifierMap.put("argument", "@ARG");
      virtualSegmentAndIdentifierMap.put("this", "@THIS");
      virtualSegmentAndIdentifierMap.put("that", "@THAT");
   }
}
