package fi.aalto.cs.apluscourses.ui.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.swing.tree.TreePath;
import org.junit.jupiter.api.Test;

class TreePathEncoderTest {
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
  void testEncode() {
    TreePathEncoder<String> encoder = spy(new TestTreePathEncoder());
    TreePath path1 = new TreePath(new Object[] {"foo", "bar", "baz"});
    TreePath path2 = new TreePath(new Object[] {"foo", "bar", 2000});

    String code1 = encoder.encode(path1);
    String code2 = encoder.encode(path2);

    assertEquals("/foo/bar/baz", code1);
    assertEquals("/foo/bar/2000", code2);

    verify(encoder, times(1)).encodeNode("/foo", "bar");
  }
}
