package fi.aalto.cs.apluscourses.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringUtilTest {

  @Test
  public void testGetArrayOfTokens() {
    assertEquals("Test get array of tokens (variable modifiers)",
        new String[] {"private", "val"}, StringUtil.getArrayOfTokens("private val", ' '));
    assertEquals("Test get array of tokens (variable annotations)",
        new String[] {"@Nullable", "@Deprecated"},
        StringUtil.getArrayOfTokens("@Nullable  @Deprecated", ' '));
  }
}
