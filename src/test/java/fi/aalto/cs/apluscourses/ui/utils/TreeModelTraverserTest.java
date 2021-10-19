package fi.aalto.cs.apluscourses.ui.utils;

import static org.junit.Assert.assertArrayEquals;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.junit.jupiter.api.Test;

public class TreeModelTraverserTest {

  @Test
  public void testTraverse() {
    DefaultMutableTreeNode child11 = new DefaultMutableTreeNode();
    DefaultMutableTreeNode child12 = new DefaultMutableTreeNode();
    DefaultMutableTreeNode child1 = new DefaultMutableTreeNode();
    child1.add(child11);
    child1.add(child12);
    DefaultMutableTreeNode child2 = new DefaultMutableTreeNode();
    DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    root.add(child1);
    root.add(child2);
    TreeModel treeModel = new DefaultTreeModel(root);
    TreeModelTraverser traverser = new TreeModelTraverser(treeModel);
    TreePath[] actuals = traverser.traverse().toArray(TreePath[]::new);
    TreePath[] expecteds = new TreePath[] {
        new TreePath(new Object[] { root }),
        new TreePath(new Object[] { root, child1 }),
        new TreePath(new Object[] { root, child1, child11 }),
        new TreePath(new Object[] { root, child1, child12 }),
        new TreePath(new Object[] { root, child2 })
    };
    assertArrayEquals(expecteds, actuals);
  }
}