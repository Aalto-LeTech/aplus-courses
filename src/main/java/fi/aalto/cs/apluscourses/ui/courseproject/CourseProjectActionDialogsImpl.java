package fi.aalto.cs.apluscourses.ui.courseproject;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class CourseProjectActionDialogsImpl implements CourseProjectActionDialogs {
  @Override
  public int showMainDialog(@NotNull Project project, @NotNull String courseName) {
    CourseProjectDialog dialog = new CourseProjectDialog(project, courseName);
    dialog.show();
    return dialog.getExitCode();
  }

  @Override
  public void showErrorDialog(@NotNull String message, @NotNull String title) {
    Messages.showErrorDialog(message, title);
  }

}
