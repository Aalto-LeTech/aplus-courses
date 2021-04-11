package fi.aalto.cs.apluscourses.ui.ideactivities;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.presentation.ideactivities.TutorialViewModel;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class StartTutorialDialog {

  @NotNull
  private final TutorialViewModel viewModel;

  public StartTutorialDialog(@NotNull TutorialViewModel viewModel, @Nullable Project project) {
    this.viewModel = viewModel;
  }

  //use a viewModel to make it unit testable (SubmissionDialog)
  public int display() {
    return JOptionPane.showConfirmDialog(null,
        new JPanel(),
        "Start Tutorial",
        JOptionPane.INFORMATION_MESSAGE);

  }


 /* @NotNull
  private final TutorialViewModel viewModel;


  public StartTutorialDialog(@NotNull TutorialViewModel viewModel, @Nullable Project project) {
    super(project);
    this.viewModel = viewModel;
    this.display();
   // this.createJButtonForAction(new TaskAction(tasks));
  }*/
 /* @Override
  protected @Nullable JComponent createCenterPanel() {
    return new JPanel();
  }*/

  // When Start is clicked: DataContext context = DataManager.getInstance().getDataContext(e.getComponent());
  //      ActionUtil.launch(TaskAction.ACTION_ID, context);

  /*@Override
  protected @Nullable JComponent createCenterPanel() {
    return null;
  }*/

  //Find the first uncompleteds Task and display that? Future feature could be to leave and restart tutorials.
  //If the user confirms, display the first Task's Window (TaskViewModel and Task)! Connect to Task's Action.

}

