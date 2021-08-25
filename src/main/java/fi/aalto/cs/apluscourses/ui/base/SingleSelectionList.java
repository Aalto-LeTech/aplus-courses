package fi.aalto.cs.apluscourses.ui.base;

import com.intellij.ui.components.JBList;
import fi.aalto.cs.apluscourses.ui.utils.Bindable;
import fi.aalto.cs.apluscourses.ui.utils.TwoWayBindable;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jetbrains.annotations.NotNull;

public class SingleSelectionList<T> extends JBList<T> {
  public final transient TwoWayBindable<SingleSelectionList<T>, T> selectionBindable =
      new TwoWayBindable<>(this, SingleSelectionList::setSelectedValue, SingleSelectionList::getSelectedValue);

  public final transient Bindable<SingleSelectionList<T>, T[]> listDataBindable =
      new Bindable<>(this, SingleSelectionList::setListData);

  private final transient Runnable submitRunnable;

  /**
   * Constructs a new SingleSelectionList.
   */
  public SingleSelectionList(@NotNull Runnable submitRunnable) {
    this.submitRunnable = submitRunnable;
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    addListSelectionListener(new SelectionListener());
  }

  public void setSelectedValue(Object anObject) {
    setSelectedValue(anObject, true);
  }

  @Override
  protected void processMouseEvent(MouseEvent e) {
    super.processMouseEvent(e);
    if (e.getClickCount() == 2) {
      submitRunnable.run();
    }
  }

  @Override
  protected void processKeyEvent(KeyEvent e) {
    super.processKeyEvent(e);
    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
      submitRunnable.run();
    }
  }

  private class SelectionListener implements ListSelectionListener {
    @Override
    public void valueChanged(ListSelectionEvent e) {
      selectionBindable.updateSource();
    }
  }
}
