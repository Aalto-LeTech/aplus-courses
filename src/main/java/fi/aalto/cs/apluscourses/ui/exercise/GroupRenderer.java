package fi.aalto.cs.apluscourses.ui.exercise;

import com.intellij.ui.SimpleListCellRenderer;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.utils.PluginResourceBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GroupRenderer extends SimpleListCellRenderer<Group> {

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
