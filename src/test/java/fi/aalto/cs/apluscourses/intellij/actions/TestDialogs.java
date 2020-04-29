package fi.aalto.cs.apluscourses.intellij.actions;

import fi.aalto.cs.apluscourses.ui.base.Dialogs;
import org.jetbrains.annotations.NotNull;

public class TestDialogs implements Dialogs {
  private boolean answerOk;

  private String lastInformationMessage = "";
  private String lastErrorMessage = "";
  private String lastOkCancelMessage = "";

  public TestDialogs(boolean answerOk) {
    this.answerOk = answerOk;
  }

  @Override
  public void showInformationDialog(@NotNull String message, @NotNull String title) {
    lastInformationMessage = message;
  }

  @Override
  public void showErrorDialog(@NotNull String message, @NotNull String title) {
    lastErrorMessage = message;
  }

  @Override
  public boolean showOkCancelDialog(@NotNull String message,
                                    @NotNull String title,
                                    @NotNull String okText,
                                    @NotNull String cancelText) {
    lastOkCancelMessage = message;
    return answerOk;
  }

  public String getLastInformationMessage() {
    return lastInformationMessage;
  }

  public String getLastErrorMessage() {
    return lastErrorMessage;
  }

  public String getLastOkCancelMessage() {
    return lastOkCancelMessage;
  }
}
