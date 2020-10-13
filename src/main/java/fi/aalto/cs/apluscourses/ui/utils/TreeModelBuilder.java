package fi.aalto.cs.apluscourses.ui.utils;

import java.util.stream.Stream;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import org.jetbrains.annotations.NotNull;

public abstract class TreeModelBuilder<T> {
  public TreeModel build(@NotNull T obj) {
    return new DefaultTreeModel(buildNode(obj));
  }

  private MutableTreeNode buildNode(@NotNull T obj) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(obj);
    childrenOf(obj).map(this::buildNode).forEach(node::add);
    return node;
  }

  @NotNull
  protected abstract Stream<? extends T> childrenOf(@NotNull T obj);

  /**
   * Returns the user object of a node created by this builder.
   *
   * @param node A node that was created by this builder.
   * @return The user object of the node.
   */
  @NotNull
  public Object getUserObject(Object node) {
    if (node instanceof DefaultMutableTreeNode) {
      return ((DefaultMutableTreeNode) node).getUserObject();
    }
    throw new IllegalArgumentException();
  }
}
