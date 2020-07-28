package fi.aalto.cs.apluscourses.ui.base;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.ui.components.JBList;
import fi.aalto.cs.apluscourses.presentation.base.ListElementViewModel;
import fi.aalto.cs.apluscourses.presentation.base.SelectableListModel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An abstract base class that co-operates with {@link ListElementViewModel}s and provides some
 * useful functionality for inheriting classes.  These features are:
 * <ul>
 *   <li>Storing selection state to presentation models.</li>
 *   <li>A sensible way to handle popup menu.</li>
 *   <li>Possibility for client objects to register as {@link ActionListener}s for so called "list
 *   actions".  This action is triggered when an element of the list is double clicked or enter key
 *   is pressed with focus on the list.</li>
 * </ul>
 *
 * @param <E> Type of the presentation model of the list elements, a subtype of
 *            {@link ListElementViewModel}.
 * @param <V> Type of the view of an list element, a subtype of {@link ListElementView}.
 */
public abstract class BaseListView<E extends ListElementViewModel<?>, V extends ListElementView>
    extends JBList<E> {

  private static final Object LIST_ACTION = new Object();
  private static final KeyStroke ENTER_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

  private final Set<ActionListener> listActionListeners = ConcurrentHashMap.newKeySet();
  private final Object popupMenuLock = new Object();
  private final V elementView;
  private JPopupMenu popupMenu;

  /**
   * A constructor.
   * @param elementView A list element view object.
   */
  public BaseListView(V elementView) {
    this.elementView = elementView;
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

  /**
   * Sets the {@link JPopupMenu} that is shown for elements.  This class currently does not support
   * different popup menus for different elements.  Should that be needed, this could be changed to
   * take a factory object instead and changing functionality accordingly.
   *
   * @param popupMenu {@link JPopupMenu}, possibly achieved by {@code ActionToolbar.getComponent()}.
   */
  public void setPopupMenu(JPopupMenu popupMenu) {
    synchronized (popupMenuLock) {
      this.popupMenu = popupMenu;
    }
  }

  /**
   * Sets the list model.  For full functionality, the list model should be an instance of a class
   * implementing {@link SelectableListModel}.  Unlike the superclass' method, this method allows
   * {@code null} to be passed for convenience.
   *
   * @param model A {@link ListModel} object or null, in which case the list model is set to an
   *              empty {@link DefaultListModel}.
   */
  @Override
  public void setModel(@Nullable ListModel<E> model) {
    if (model == null) {
      model = new DefaultListModel<>();
    }
    ListModel<E> finalModel = model;
    ApplicationManager.getApplication().invokeLater(
        () -> {
          super.setModel(finalModel);
          if (finalModel instanceof SelectableListModel) {
            setSelectionModel(((SelectableListModel<E>) finalModel).getSelectionModel());
          }
        },
        ModalityState.any()
    );
  }

  /**
   * Registers an {@link ActionListener} to listen to so called "list actions" that are triggered
   * when an element of list is "applied" either by double click or enter key.  Note that the
   * {@link BaseListView} maintain strong references to its listeners, so
   * {@code removeListActionListener} should be called to allow the {@link BaseListView} to be
   * garbage collected.  Subsequent calls to this method with the same argument have no effect.
   *
   * @param listener An {@link ActionListener}.
   */
  public void addListActionListener(ActionListener listener) {
    listActionListeners.add(listener);
  }

  /**
   * Removes the given {@link ActionListener} of the set of list action listeners.  If the listener
   * is not in that set, this method has no effect.
   * @param listener An {@link ActionListener}.
   */
  public void removeListActionListener(ActionListener listener) {
    listActionListeners.remove(listener);
  }

  /**
   * When implemented in a subclass, this method should update the element view to represent the
   * state of the given element.  Please note that this method is called repeatedly by the UI so it
   * should be fast.
   *
   * @param element A presentation model of an element.
   */
  protected abstract void updateElementView(@NotNull V elementView, @NotNull E element);

  @NotNull
  private JComponent getRendererForElement(@Nullable E element) {
    if (element != null) {
      updateElementView(elementView, element);
    }
    return elementView.getRenderer();
  }

  protected void showPopupMenu(@NotNull Point location) {
    synchronized (popupMenuLock) {
      if (popupMenu != null) {
        popupMenu.show(this, location.x, location.y);
      }
    }
  }


  private void onListActionPerformed() {
    ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null);
    listActionListeners.forEach(listener -> listener.actionPerformed(event));
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
          showPopupMenu(mouseEvent.getPoint());
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
