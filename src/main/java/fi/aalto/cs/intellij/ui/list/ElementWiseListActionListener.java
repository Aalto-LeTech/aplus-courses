package fi.aalto.cs.intellij.ui.list;

import java.util.function.Consumer;
import javax.swing.ListModel;

public class ElementWiseListActionListener<E> implements ListActionListener<E> {

  private final Consumer<E> consumer;

  public ElementWiseListActionListener(Consumer<E> consumer) {
    this.consumer = consumer;
  }

  @Override
  public void listActionPerformed(ListActionEvent<E> event) {
    ListModel<E> listModel = event.getList().getModel();
    if (listModel != null) {
      for (int index : event.getIndices()) {
        E element = listModel.getElementAt(index);
        if (element != null) {
          consumer.accept(element);
        }
      }
    }
  }
}
