package fi.aalto.cs.apluscourses.ui.base;

import fi.aalto.cs.apluscourses.presentation.base.Filterable;
import fi.aalto.cs.apluscourses.utils.CollectionUtil;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class OurTreeNode extends DefaultMutableTreeNode implements Filterable.Listener {

  private boolean visible;

  public OurTreeNode(Object userObject) {
    super(userObject);
    if (userObject instanceof Filterable) {
      ((Filterable) userObject).addVisibilityListener(this);
    }
  }

  @Override
  public void visibilityChanged(boolean isVisible) {
    this.visible = isVisible;
  }

  @Override
  public TreeNode getChildAt(int index) {
    return (TreeNode) CollectionUtil.getNth(children.iterator(), OurTreeNode::isNodeVisible, index);
  }

  @Override
  public int getChildCount() {
    return super.getChildCount();
  }

  @Override
  public int getIndex(TreeNode treeNode) {
    return super.getIndex(treeNode);
  }

  private static boolean isNodeVisible(Object node) {
    return !(node instanceof OurTreeNode) || ((OurTreeNode) node).visible;
  }
}
