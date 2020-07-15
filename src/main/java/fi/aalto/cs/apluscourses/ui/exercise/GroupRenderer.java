package fi.aalto.cs.apluscourses.ui.exercise;

import com.intellij.ui.SimpleListCellRenderer;
import fi.aalto.cs.apluscourses.model.Group;
import javax.swing.JList;
import org.jetbrains.annotations.NotNull;

public class GroupRenderer extends SimpleListCellRenderer<Group> {

  @Override
  public void customize(@NotNull JList<? extends Group> list,
                        @NotNull Group group,
                        int index,
                        boolean selected,
                        boolean hasFocus) {
    setText(String.join(", ", group.getMemberNames()));
  }
}
