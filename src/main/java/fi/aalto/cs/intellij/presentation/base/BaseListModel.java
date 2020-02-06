package fi.aalto.cs.intellij.presentation.base;

import java.util.List;
import java.util.stream.Collectors;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An abstract extension to {@link ListModel} that co-operates with {@link ListElementModel}.
 * Provides the following functionality:
 * <ul>
 * <li>selection state to be stored inside the element objects</li>
 * <li>the view to be informed about changes in the presentation model</li>
 * </ul>
 * @param <E> Type of the list elements (presentation models), subtype of {@link ListElementModel}
 */
public class BaseListModel<E extends ListElementModel<?>> extends AbstractListModel<E>
    implements SelectableListModel {

  @NotNull
  private final ListSelectionModel selectionModel;
  @NotNull
  private final List<E> elements;

  /**
   * A constructor.
   * @param elements List of elements.  Note that the list should not be changed after this
   *                 constructor call.
   */
  public BaseListModel(@NotNull List<E> elements) {
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

  /**
   * Get currently selected elements as a list.  The list is a snapshot of the current selection
   * state and is not updated after this method call.
   *
   * @return A {@link List}.
   */
  public List<E> getSelectedElements() {
    return elements
        .stream()
        .filter(ListElementModel::isSelected)
        .collect(Collectors.toList());
  }

  @Override
  @NotNull
  public ListSelectionModel getSelectionModel() {
    return selectionModel;
  }

  @Override
  public int getSize() {
    return elements.size();
  }

  @Override
  @Nullable
  public E getElementAt(int i) {
    try {
      return elements.get(i);
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
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
        E element = BaseListModel.this.getElementAt(index);
        if (element != null) {
          boolean selected = isSelectedIndex(index);
          element.setSelected(selected);
        }
      }
    }
  }

}
