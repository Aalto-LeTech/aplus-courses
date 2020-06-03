package fi.aalto.cs.apluscourses.ui.courseproject;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import fi.aalto.cs.apluscourses.presentation.CourseProjectViewModel;
import org.jetbrains.annotations.NotNull;

public class CourseProjectActionDialogsImpl implements CourseProjectActionDialogs {
  @Override
  public void showMainDialog(@NotNull Project project,
                            @NotNull CourseProjectViewModel courseProjectViewModel) {
    CourseProjectView dialog = new CourseProjectView(project, courseProjectViewModel);
    dialog.show();
  }

  @Override
  public void showErrorDialog(@NotNull String message, @NotNull String title) {
    Messages.showErrorDialog(message, title);
  }

}
