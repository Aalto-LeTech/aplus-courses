package fi.aalto.cs.apluscourses.ui.courseproject;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CourseProjectActionDialogsImpl implements CourseProjectActionDialogs {
  @Override
  public int showMainDialog(@NotNull Project project,
                            @NotNull String courseName,
                            @Nullable String currentlyImportedSettings) {
    CourseProjectDialog dialog
        = new CourseProjectDialog(project, courseName, currentlyImportedSettings);
    dialog.show();
    return dialog.getExitCode();
  }

  @Override
  public void showErrorDialog(@NotNull String message, @NotNull String title) {
    Messages.showErrorDialog(message, title);
  }

}
