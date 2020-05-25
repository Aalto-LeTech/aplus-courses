package fi.aalto.cs.apluscourses.ui.courseproject;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface CourseProjectActionDialogs {

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
