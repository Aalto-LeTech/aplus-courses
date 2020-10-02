package fi.aalto.cs.apluscourses.ui.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import fi.aalto.cs.apluscourses.presentation.base.FilterableTree;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

public class CompositeTreeNodeTest {

  public static class TestUserObject implements FilterableTree {

    private final boolean visible;
    private final List<TestUserObject> children;

    public TestUserObject(boolean visible, List<TestUserObject> children) {
      this.visible = visible;
      this.children = children;
    }

    @Override
    public boolean isVisible() {
      return visible;
    }

    @Override
    public @NotNull List<TestUserObject> getChildren() {
      return children;
    }
  }

  TestUserObject child0;
  TestUserObject hiddenChild;
  TestUserObject child1;
  TestUserObject root;
  CompositeTreeNode node;

  /**
   * Called before each test.
   */
  @Before
  public void setUp() {
    child0 = new TestUserObject(true, Collections.emptyList());
    hiddenChild = new TestUserObject(false, Collections.emptyList());
    child1 = new TestUserObject(true, Collections.emptyList());
    root = new TestUserObject(true, Arrays.asList(child0, hiddenChild, child1));
    node = CompositeTreeNode.create(root);
  }

  @Test
  public void testGetUserObject() {
    assertSame(root, node.getUserObject());
  }

  @Test
  public void testGetChildAt() {
    assertSame(child0, node.getChildAt(0).getUserObject());
    assertSame(child1, node.getChildAt(1).getUserObject());
  }

  @Test
  public void testGetChildCount() {
    assertEquals(2, node.getChildCount());
    assertEquals(0, node.getChildAt(0).getChildCount());
    assertEquals(0, node.getChildAt(1).getChildCount());

  }

  @Test
  public void testGetParent() {
    assertNull(node.getParent());
    assertSame(node, node.getChildAt(0).getParent());
    assertSame(node, node.getChildAt(1).getParent());
  }

  @Test
  public void testGetIndex() {
    assertEquals(0, node.getIndex(node.getChildAt(0)));
    assertEquals(1, node.getIndex(node.getChildAt(1)));
    assertEquals(-1, node.getIndex(CompositeTreeNode.create(hiddenChild)));
  }

  @Test
  public void testGetAllowsChildren() {
    assertTrue(node.getAllowsChildren());
    assertFalse(node.getChildAt(0).getAllowsChildren());
    assertFalse(node.getChildAt(1).getAllowsChildren());
  }

  @Test
  public void testIsLeaf() {
    assertFalse(node.isLeaf());
    assertTrue(node.getChildAt(0).isLeaf());
    assertTrue(node.getChildAt(1).isLeaf());
  }

  @Test
  public void testChildren() {
    List<?> childNodes = Collections.list(node.children());
    assertEquals(2, childNodes.size());
    assertSame(node.getChildAt(0), childNodes.get(0));
    assertSame(node.getChildAt(1), childNodes.get(1));
  }
}
