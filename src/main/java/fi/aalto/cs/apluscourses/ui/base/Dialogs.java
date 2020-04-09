package fi.aalto.cs.apluscourses.ui.base;

import org.jetbrains.annotations.NotNull;

public interface Dialogs {
  void showInformationDialog(@NotNull String message, @NotNull String title);

  void showErrorDialog(@NotNull String message, @NotNull String title);

  /**
   * Returns true if the user selects ok, false otherwise.
   */
  boolean showOkCancelDialog(@NotNull String message,
                             @NotNull String title,
                             @NotNull String okText,
                             @NotNull String cancelText);
}
