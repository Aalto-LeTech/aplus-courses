package fi.aalto.cs.apluscourses.ui;

import com.intellij.openapi.ui.Messages;
import fi.aalto.cs.apluscourses.ui.base.Dialogs;
import org.jetbrains.annotations.NotNull;

public class IntelliJDialogs implements Dialogs {
  @Override
  public void showInformationDialog(@NotNull String message, @NotNull String title) {
    Messages.showInfoMessage(message, title);
  }

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
}
