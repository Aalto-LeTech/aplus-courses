package fi.aalto.cs.apluscourses.utils;

import java.util.function.IntPredicate;

public class FunctionUtil {
  private FunctionUtil() {

  }

  public static IntPredicate lessThanOrEqualTo(int x) {
    return y -> y <= x;
  }
}
