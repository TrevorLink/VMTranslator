package com.noctis.vm.translator.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Noctis
 * @date 2025/02/02
 */
public class VMConstants {

   public static final String INSTRUCTION_PATTERN = "^(push|pop)\\s(constant|local|argument|this|that|static|temp|pointer)\\s\\d+$";

   public static Set<String> ARITHMETIC_COMMAND_SET = new HashSet<>(Arrays.asList("add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"));

   public static String PUSH_COMMAND_PREFIX = "push";

   public static String POP_COMMAND_PREFIX = "pop";

   public static String COMMENT_IDENTIFIER = "//";

   public static String RESULT_ASM_FILE_SUFFIX = ".asm";
}
