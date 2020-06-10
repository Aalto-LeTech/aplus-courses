package fi.aalto.cs.apluscourses.ui.courseproject;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.presentation.CourseProjectViewModel;
import org.jetbrains.annotations.NotNull;

public interface CourseProjectActionDialogs {

  boolean showMainDialog(@NotNull Project project,
                         @NotNull CourseProjectViewModel courseProjectViewModel);

  void showErrorDialog(@NotNull String message, @NotNull String title);

}
