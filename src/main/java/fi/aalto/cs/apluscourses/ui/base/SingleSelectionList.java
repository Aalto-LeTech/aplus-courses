package fi.aalto.cs.apluscourses.ui.base;

import com.intellij.ui.components.JBList;
import fi.aalto.cs.apluscourses.ui.utils.TwoWayBindable;
import java.util.List;
import javax.swing.ListSelectionModel;
import org.jetbrains.annotations.NotNull;

public class SingleSelectionList<T> extends JBList<T> {
  public final TwoWayBindable<SingleSelectionList<T>, T> selectionBindable =
      new TwoWayBindable<>(this, (a, b) -> setSelectedValue(a, true), SingleSelectionList::getSelectedValue);

  public SingleSelectionList(@NotNull List<T> items) {
    super(items);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  }
}
