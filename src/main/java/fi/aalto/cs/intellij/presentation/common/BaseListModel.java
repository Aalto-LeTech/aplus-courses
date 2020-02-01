package fi.aalto.cs.intellij.presentation.common;

import java.util.List;
import java.util.stream.Collectors;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jetbrains.annotations.NotNull;

public class BaseListModel<E extends ListElementModel> extends AbstractListModel<E> {

  @NotNull
  private final ListSelectionModel selectionModel;
  private final List<E> elements;

  public BaseListModel(List<E> elements) {
    this.selectionModel = new SelectionModel();
    this.elements = elements;
    for (E element : elements) {
      element.setListModel(this);
    }
    index();
  }

  private void index() {
    int index = 0;
    for (E element : elements) {
      element.setIndex(index++);
    }
  }

  public void onElementChanged(int index) {
    fireContentsChanged(this, index, index);
  }

  public List<E> getSelectedElements() {
    return elements
        .stream()
        .filter(ListElementModel::isSelected)
        .collect(Collectors.toList());
  }

  @NotNull
  public ListSelectionModel getSelectionModel() {
    return selectionModel;
  }

  @Override
  public int getSize() {
    return elements.size();
  }

  @Override
  public E getElementAt(int i) {
    return elements.get(i);
  }

  private class SelectionModel extends DefaultListSelectionModel implements ListSelectionListener {

    public SelectionModel() {
      addListSelectionListener(this);
    }

    @Override
    public void valueChanged(ListSelectionEvent selectionEvent) {
      int firstIndex = selectionEvent.getFirstIndex();
      int lastIndex = selectionEvent.getLastIndex();
      for (int index = firstIndex; index <= lastIndex; index++) {
        ListElementModel element = BaseListModel.this.getElementAt(index);
        boolean selected = isSelectedIndex(index);
        element.setSelected(selected);
      }
    }
  }

}
