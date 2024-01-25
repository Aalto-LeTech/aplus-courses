package fi.aalto.cs.apluscourses.ui;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.ui.Messages;

public class DuplicateSubmissionDialog {

  private DuplicateSubmissionDialog() {

  }

  /**
   * Displays a dialog asking the user whether they really want to submit a duplicate submissions.
   * @return True if the user wants to proceed with the submission.
   */
  public static boolean showDialog() {
    final String[] options = { getText("ui.duplicateDialog.yesOption"), getText("ui.duplicateDialog.noOption") };

    return Messages.showDialog(getText("ui.duplicateDialog.content"),
        getText("ui.duplicateDialog.title"),
        options,
        1,
        null) == 0;
  }
}
