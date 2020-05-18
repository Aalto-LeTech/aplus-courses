package fi.aalto.cs.apluscourses.ui.courseproject;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class CourseProjectActionDialogsImpl implements CourseProjectActionDialogs {
  @Override
  public void showErrorDialog(@NotNull String message, @NotNull String title) {
    Messages.showErrorDialog(message, title);
  }

  @Override
  public boolean showOkCancelDialog(@NotNull String message,
                                    @NotNull String title,
                                    @NotNull String okText,
                                    @NotNull String cancelText) {
    return Messages.OK == Messages.showOkCancelDialog(
        message, title, okText, cancelText, Messages.getQuestionIcon());
  }

  @Override
  public boolean showImportIdeSettingsDialog(@NotNull Project project) {
    return new ImportIdeSettingsDialog(project).showAndGet();
  }
}
