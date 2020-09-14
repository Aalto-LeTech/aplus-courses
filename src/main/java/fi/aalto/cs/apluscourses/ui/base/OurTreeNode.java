package fi.aalto.cs.apluscourses.ui.base;

import fi.aalto.cs.apluscourses.presentation.base.Filterable;
import fi.aalto.cs.apluscourses.utils.CollectionUtil;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import sourcecode.Impls;

public class OurTreeNode extends DefaultMutableTreeNode implements Filterable.Listener {

  private final AtomicBoolean visible = new AtomicBoolean();

  public OurTreeNode(Object userObject) {
    super(userObject);
  }

  @Override
  public void visibilityChanged(boolean isVisible) {
    this.visible.set(isVisible);
  }

  private Stream<TreeNode> streamVisibleChildren() {
    return ((Stream<?>) Optional.ofNullable(children)
        .map(Collection::stream)
        .orElseGet(Stream::empty))
        .map(TreeNode.class::cast)
        .filter(OurTreeNode::isNodeVisible);
  }

  @Override
  public TreeNode getChildAt(int index) {
    return streamVisibleChildren()
        .skip(index)
        .findFirst()
        .orElseThrow(ArrayIndexOutOfBoundsException::new);
  }

  @Override
  public int getChildCount() {
    return (int) streamVisibleChildren().count();
  }

  @Override
  public void add(MutableTreeNode node) {
    if (node != null && node.getParent() == this) {
      insert(node, super.getChildCount() - 1);
    } else {
      insert(node, super.getChildCount());
    }
  }

  @Override
  public int getIndex(TreeNode treeNode) {
    return CollectionUtil.indexOf(streamVisibleChildren().iterator(), treeNode);
  }

  private static boolean isNodeVisible(Object node) {
    return !(node instanceof OurTreeNode) || ((OurTreeNode) node).visible.get();
  }
}
