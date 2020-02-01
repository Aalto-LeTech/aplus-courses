package fi.aalto.cs.intellij.ui.list;

import com.intellij.ui.components.JBList;

public class ListActionEvent<E> {
  private final JBList<E> list;
  private final int[] indices;

  public ListActionEvent(JBList<E> list, int[] indices) {
    this.list = list;
    this.indices = indices;
  }

  public JBList<E> getList() {
    return list;
  }

  public int[] getIndices() {
    return indices;
  }
}
