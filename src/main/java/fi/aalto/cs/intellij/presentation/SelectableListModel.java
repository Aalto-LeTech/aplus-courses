package fi.aalto.cs.intellij.presentation;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jetbrains.annotations.NotNull;

public abstract class SelectableListModel<E extends Selectable> extends AbstractListModel<E> {

  @NotNull
  private final ListSelectionModel selectionModel;

  public SelectableListModel() {
    this.selectionModel = new SelectionModel();
  }

  @NotNull
  public ListSelectionModel getSelectionModel() {
    return selectionModel;
  }

  public class SelectionModel extends DefaultListSelectionModel implements ListSelectionListener {

    public SelectionModel() {
      addListSelectionListener(this);
    }

    @Override
    public void valueChanged(ListSelectionEvent selectionEvent) {
      int firstIndex = selectionEvent.getFirstIndex();
      int lastIndex = selectionEvent.getLastIndex();
      for (int index = firstIndex; index <= lastIndex; index++) {
        Selectable selectable = SelectableListModel.this.getElementAt(index);
        boolean selected = isSelectedIndex(index);
        selectable.setSelected(selected);
      }
    }
  }

}
