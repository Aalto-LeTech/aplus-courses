package fi.aalto.cs.apluscourses.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

public class StringUtilTest {

  @Test
  public void testGetArrayOfTokens() {
    assertArrayEquals(new String[] {"private", "val"},
        StringUtil.getArrayOfTokens("private val", ' '),
        "Test get array of tokens (variable modifiers)");
    assertArrayEquals(new String[] {"@Nullable", "@Deprecated"},
        StringUtil.getArrayOfTokens("@Nullable  @Deprecated", ' '),
        "Test get array of tokens (variable annotations)");
  }
}
