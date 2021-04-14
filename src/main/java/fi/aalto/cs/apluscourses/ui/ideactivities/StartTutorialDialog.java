package fi.aalto.cs.apluscourses.ui.ideactivities;

import fi.aalto.cs.apluscourses.presentation.ideactivities.TutorialViewModel;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;

public class StartTutorialDialog {

  public StartTutorialDialog() {
  }
  public static void createAndShow(@NotNull TutorialViewModel viewModel) {
    int result = new StartTutorialDialog().display();
    if (result == JOptionPane.OK_OPTION) {
      viewModel.startNextTask();
    }
  }

  //use a viewModel to make it unit testable (SubmissionDialog)
  public int display() {
    return JOptionPane.showConfirmDialog(null,
        new JPanel(),
        "Start Tutorial",
        JOptionPane.INFORMATION_MESSAGE);
  }


  //Find the first uncompleteds Task and display that? Future feature could be to leave and restart tutorials.
  //If the user confirms, display the first Task's Window.

}

