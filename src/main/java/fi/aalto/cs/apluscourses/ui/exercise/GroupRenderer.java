package fi.aalto.cs.apluscourses.ui.exercise;

import com.intellij.ui.SimpleListCellRenderer;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.utils.PluginResourceBundle;
import javax.swing.JList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GroupRenderer extends SimpleListCellRenderer<Group> {

  private static final long serialVersionUID = 3192396227281974523L;

  @Override
  public void customize(@NotNull JList<? extends Group> list,
                        @Nullable Group group,
                        int index,
                        boolean selected,
                        boolean hasFocus) {
    if (group == null) {
      setText(
          PluginResourceBundle.getText("ui.toolWindow.subTab.exercises.submission.selectGroup"));
    } else {
      setText(String.join(", ", group.getMemberNames()));
    }
  }
}
