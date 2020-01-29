package fi.aalto.cs.intellij.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;

public class ListElementView<E> extends JLabel {
  private static final Object LIST_ACTION = new Object();
  private static final KeyStroke ENTER_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

  private final E element;
  private  final ListActionListener<E> actionListener;

  public ListElementView(E element, ListActionListener<E> actionListener) {
    this.element = element;
    this.actionListener = actionListener;
    getInputMap(JComponent.WHEN_FOCUSED).put(ENTER_KEY_STROKE, LIST_ACTION);
    getActionMap().put(LIST_ACTION, new RelayAction(actionEvent ->
        actionListener.listActionPerformed(new ListActionEvent<>(this, element))));
  }

  private class ListMouseListener extends AbstractMouseListener {

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
      if (mouseEvent.getClickCount() == 2) {
        actionListener.listActionPerformed(new ListActionEvent<>(ListElementView.this, element));
      }
    }
  }

  public static class ListActionListener<E>  {
    void listActionPerformed(ListActionEvent<E> actionEvent) {

    }
  }

  public static class ListActionEvent<E> extends ActionEvent {
    private static final String COMMAND = "list";

    public ListActionEvent(ListElementView source, E element) {
      super(source, ActionEvent.ACTION_PERFORMED, COMMAND);
    }
  }
}
