package fi.aalto.cs.intellij.ui.list;

import com.intellij.ui.components.JBList;
import fi.aalto.cs.intellij.ui.common.AbstractMouseListener;
import fi.aalto.cs.intellij.ui.common.RelayAction;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

public class ListUtil {

  private static final Object LIST_ACTION = new Object();
  private static final KeyStroke ENTER_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

  public static <E> void addListActionListener(JBList<E> list, ListActionListener<E> listener) {
    list.addMouseListener(new ListMouseListener<>(list, listener));
    list.getInputMap(JComponent.WHEN_FOCUSED).put(ENTER_KEY_STROKE, LIST_ACTION);
    list.getActionMap().put(LIST_ACTION, new RelayAction(event ->
        listener.listActionPerformed(new ListActionEvent<>(list, list.getSelectedIndices()))));
  }

  private ListUtil() {

  }

  private static class ListMouseListener<E> extends AbstractMouseListener {

    private JBList<E> list;
    private ListActionListener<E> listener;

    public ListMouseListener(JBList<E> list, ListActionListener<E> listener) {
      this.list = list;
      this.listener = listener;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
      if (mouseEvent.getClickCount() == 2) {
        Point location = mouseEvent.getPoint();
        int index = list.locationToIndex(location);
        Rectangle bounds = list.getCellBounds(index, index);
        if (bounds != null && bounds.contains(location)) {
          listener.listActionPerformed(new ListActionEvent<>(list, new int[] { index }));
        }
      }
    }
  }
}
