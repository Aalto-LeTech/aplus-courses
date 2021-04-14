package fi.aalto.cs.apluscourses.ui.ideactivities;

import fi.aalto.cs.apluscourses.presentation.ideactivities.TutorialViewModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public class StartTutorialDialog {

  /**
   * Creates and displays a confirmation window for starting the Tutorial.
   * @param viewModel TutorialViewModel of the current Tutorial
   */
  public static void createAndShow(@NotNull TutorialViewModel viewModel) {
    int result = new StartTutorialDialog().display();
    //If the user confirms, display the first Task's Window.
    if (result == JOptionPane.OK_OPTION) {
      viewModel.startNextTask();
    }
  }

  /**
   * Shows a window asking the user to confirm the start of the Tutorial.
   * @return
   */
  public int display() {
    return JOptionPane.showConfirmDialog(null,
        new JPanel(),
        "Start Tutorial",
            JOptionPane.YES_NO_CANCEL_OPTION);
  }


  //Find the first uncompleteds Task and display that? ->
  // Future feature could be to leave and restart tutorials.

}

