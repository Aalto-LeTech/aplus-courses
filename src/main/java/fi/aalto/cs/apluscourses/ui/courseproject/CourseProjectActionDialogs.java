package fi.aalto.cs.apluscourses.ui.courseproject;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface CourseProjectActionDialogs {

  int CANCEL = 0;
  int OK_WITH_RESTART = CANCEL + 1;
  int OK_WITHOUT_RESTART = OK_WITH_RESTART + 1;
  int OK_WITH_OPT_OUT = OK_WITHOUT_RESTART + 1;

  int showMainDialog(@NotNull Project project, @NotNull String courseName);

  void showErrorDialog(@NotNull String message, @NotNull String title);

  /**
   * Returns true if the user selects ok, false otherwise.
   */
  boolean showOkCancelDialog(@NotNull String message,
                             @NotNull String title,
                             @NotNull String okText,
                             @NotNull String cancelText);

  boolean showImportIdeSettingsDialog(@NotNull Project project);
}
