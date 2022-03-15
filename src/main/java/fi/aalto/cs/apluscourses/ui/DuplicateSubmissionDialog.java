package fi.aalto.cs.apluscourses.ui;

import javax.swing.JOptionPane;

public class DuplicateSubmissionDialog {

  private DuplicateSubmissionDialog() {

  }

  /**
   * Displays a dialog asking the user whether they really want to submit a duplicate submissions.
   * @return True if the user wants to proceed with the submission.
   */
  public static boolean showDialog() {
    final String[] options = { "Submit anyway", "Don't submit" };

    return JOptionPane.showOptionDialog(null,
        "In the past, you have already made the exact same submission for this exercise.\nDo you wish to submit anyway?",
        "Duplicate submission detected",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[1]
    ) == 0;
  }
}
