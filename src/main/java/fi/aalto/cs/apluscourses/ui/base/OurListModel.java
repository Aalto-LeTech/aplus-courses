package fi.aalto.cs.apluscourses.ui.base;

import fi.aalto.cs.apluscourses.presentation.base.ListElementViewModel;
import fi.aalto.cs.apluscourses.presentation.base.SelectableListModel;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.ListUtil;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OurListModel<E extends ListElementViewModel<?>>
        extends AbstractListModel<E>
        implements SelectableListModel<E> {
  @NotNull
  private final ListSelectionModel selectionModel = new SelectionModel();

  private final List<E> elements;

  public OurListModel(@NotNull List<E> elements) {
    this.elements = elements;
    ListUtil.forEachWithIndex(elements, this::registerListenerForElement);
  }

  private void registerListenerForElement(E element, int index) {
    element.changed.addListener(this, new ElementChangedEventHandler<>(index));
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

  private static class ElementChangedEventHandler<E extends ListElementViewModel<?>>
          implements Event.Callback<OurListModel<E>> {
    private final int index;

    public ElementChangedEventHandler(int index) {
      this.index = index;
    }

    @Override
    public void callback(@NotNull OurListModel<E> listener) {
      listener.fireContentsChanged(listener, index, index);
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
        E element = OurListModel.this.getElementAt(index);
        if (element != null) {
          boolean selected = isSelectedIndex(index);
          element.setSelected(selected);
        }
      }
    }
  }
}
