package fi.aalto.cs.apluscourses.ui.base;

import com.intellij.ui.components.JBList;
import fi.aalto.cs.apluscourses.ui.utils.Bindable;
import fi.aalto.cs.apluscourses.ui.utils.TwoWayBindable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class SingleSelectionList<T> extends JBList<T> {
  public final transient TwoWayBindable<SingleSelectionList<T>, T> selectionBindable =
      new TwoWayBindable<>(this, SingleSelectionList::setSelectedValue, SingleSelectionList::getSelectedValue);

  public final transient Bindable<SingleSelectionList<T>, T[]> listDataBindable =
      new Bindable<>(this, SingleSelectionList::setListData);

  /**
   * Constructs a new SingleSelectionListener.
   */
  public SingleSelectionList() {
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    addListSelectionListener(new SelectionListener());
  }

  public void setSelectedValue(Object anObject) {
    setSelectedValue(anObject, true);
  }

  private class SelectionListener implements ListSelectionListener {
    @Override
    public void valueChanged(ListSelectionEvent e) {
      selectionBindable.updateSource();
    }
  }
}
