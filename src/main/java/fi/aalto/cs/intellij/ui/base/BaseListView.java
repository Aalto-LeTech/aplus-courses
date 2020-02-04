package fi.aalto.cs.intellij.ui.base;

import com.intellij.ui.components.JBList;
import fi.aalto.cs.intellij.presentation.base.BaseListModel;
import fi.aalto.cs.intellij.presentation.base.ListElementModel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseListView<E extends ListElementModel<?>, V>
    extends JBList<E> {

  private static final Object LIST_ACTION = new Object();
  private static final KeyStroke ENTER_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

  private final Set<ActionListener> listActionListeners = ConcurrentHashMap.newKeySet();
  private final ConcurrentMap<E, V> views = new ConcurrentHashMap<>();
  private final Object popupMenuLock = new Object();
  private JPopupMenu popupMenu;

  public BaseListView() {
    addMouseListener(new ListMouseListener());
    getInputMap(JComponent.WHEN_FOCUSED).put(ENTER_KEY_STROKE, LIST_ACTION);
    getActionMap().put(LIST_ACTION, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        onListActionPerformed();
      }
    });
    installCellRenderer(this::getRendererForElement);
  }

  public void setPopupMenu(JPopupMenu popupMenu) {
    synchronized (popupMenuLock) {
      this.popupMenu = popupMenu;
    }
  }

  @Override
  public void setModel(ListModel<E> model) {
    super.setModel(model);
    if (model instanceof BaseListModel) {
      setSelectionModel(((BaseListModel<E>) model).getSelectionModel());
    }
  }

  @NotNull
  protected abstract V createElementView(E element);

  protected abstract void updateElementView(V view, E element);

  @NotNull
  protected abstract JComponent renderElementView(V view);

  @NotNull
  private JComponent getRendererForElement(E element) {
    V view = views.computeIfAbsent(element, this::createElementView);
    if (element.checkIfChanged()) {
      updateElementView(view, element);
    }
    return renderElementView(view);
  }

  public void addListActionListener(ActionListener listener) {
    listActionListeners.add(listener);
  }

  private void onListActionPerformed() {
    ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null);
    listActionListeners.forEach(listener -> listener.actionPerformed(event));
  }

  private void showPopupMenu(int index, @Nullable Point location) {
    if (location == null) {
      location = indexToLocation(index);
      if (location == null) {
        return;
      }
    }
    synchronized (popupMenuLock) {
      if (popupMenu != null) {
        popupMenu.show(this, location.x, location.y);
      }
    }
  }

  private class ListMouseListener extends MouseAdapter {

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
      selectIfPopupMenuTriggered(mouseEvent);
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
      selectIfPopupMenuTriggered(mouseEvent);
    }

    private void selectIfPopupMenuTriggered(MouseEvent mouseEvent) {
      if (mouseEvent.isPopupTrigger()) {
        int index = getIndex(mouseEvent);
        if (index >= 0) {
          setSelectedIndex(index);
          showPopupMenu(index, mouseEvent.getPoint());
        }
      }
    }

    private int getIndex(MouseEvent mouseEvent) {
      Point location = mouseEvent.getPoint();
      int index = locationToIndex(location);
      Rectangle bounds = getCellBounds(index, index);
      return bounds != null && bounds.contains(location) ? index : -1;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
      if (mouseEvent.getClickCount() == 2 && getIndex(mouseEvent) >= 0) {
        onListActionPerformed();
      }
    }
  }
}
