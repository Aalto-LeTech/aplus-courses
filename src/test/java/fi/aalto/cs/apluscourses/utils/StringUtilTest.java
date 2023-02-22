package fi.aalto.cs.apluscourses.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringUtilTest {

  @Test
  void testGetArrayOfTokens() {
    Assertions.assertArrayEquals(new String[] {"private", "val"}, StringUtil.getArrayOfTokens("private val", ' '),
        "Test get array of tokens (variable modifiers)");
    Assertions.assertArrayEquals(new String[] {"@Nullable", "@Deprecated"},
        StringUtil.getArrayOfTokens("@Nullable  @Deprecated", ' '), "Test get array of tokens (variable annotations)");
  }
}
