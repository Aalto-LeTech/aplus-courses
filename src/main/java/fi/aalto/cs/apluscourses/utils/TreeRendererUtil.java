package fi.aalto.cs.apluscourses.utils;

import javax.swing.tree.DefaultMutableTreeNode;

public class TreeRendererUtil {
  private TreeRendererUtil() {
    
  }

  /**
   * Sometimes {@code customizeCellRenderer} is called with a value
   * that is just some placeholder object for tree root.
   * This method recognizes such cases.
   *
   * @param value A parameter passed to {@code customizeCellRenderer}
   * @return True, if node is irrelevant and should be ignored, otherwise false.
   */
  public static boolean isIrrelevantNode(Object value) {
    // That irrelevant root has no parent
    return value instanceof DefaultMutableTreeNode
        && ((DefaultMutableTreeNode) value).getParent() == null;
  }
}
