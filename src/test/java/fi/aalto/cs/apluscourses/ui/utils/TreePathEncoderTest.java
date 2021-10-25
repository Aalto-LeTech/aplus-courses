package fi.aalto.cs.apluscourses.ui.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.swing.tree.TreePath;

import org.junit.jupiter.api.Test;

public class TreePathEncoderTest {
  private static class TestTreePathEncoder extends TreePathEncoder<String> {
    @Override
    protected String emptyCode() {
      return "";
    }

    @Override
    protected String encodeNode(String parentCode, Object node) {
      return parentCode + "/" + node.toString();
    }
  }

  @Test
  public void testEncode() {
    TreePathEncoder<String> encoder = new TestTreePathEncoder();
    TreePath path = new TreePath(new Object[] { "foo", "bar", "baz" });
    String code = encoder.encode(path);

    assertEquals("/foo/bar/baz", code);
  }
}