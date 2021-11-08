package fi.aalto.cs.apluscourses.utils;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class StringUtilTest {

  @Test
  public void testGetArrayOfTokens() {
    assertArrayEquals("Test get array of tokens (variable modifiers)",
        new String[] {"private", "val"}, StringUtil.getArrayOfTokens("private val", ' '));
    assertArrayEquals("Test get array of tokens (variable annotations)",
        new String[] {"@Nullable", "@Deprecated"},
        StringUtil.getArrayOfTokens("@Nullable  @Deprecated", ' '));
  }
}
