package fi.aalto.cs.apluscourses.ui.ideactivities;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.ui.components.JBLabel;
import fi.aalto.cs.apluscourses.presentation.ideactivities.TaskViewModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jetbrains.annotations.NotNull;

public class TaskView {

  private JPanel main;
  protected JLabel label;
  private final TaskViewModel taskViewModel;

  /**
   * Constructor.
   * @param viewModel the TaskViewModel used
   */
  public TaskView(@NotNull TaskViewModel viewModel) {
    main = new JPanel();
    label = new JBLabel();
    label.setText(viewModel.getTaskDescription());
    main.add(label);
    this.taskViewModel = viewModel;
  }

  /**
   * Method used to generate and display a TaskView
   * according to the given taskViewModel.
   * @param taskViewModel the TaskViewModel to be used from the TaskView
   */
  //Also, send termination signal in order to cancel Tutorial and free up resources!
  public static void createAndShow(@NotNull TaskViewModel taskViewModel) {
    new TaskView(taskViewModel).show();
  }

  /**
   * Displays and/or modifies the UI of the TaskVIew.
   */
  public void show() {
    ApplicationManager.getApplication().invokeLater(() -> {
      if (taskViewModel != null) {
        JOptionPane.showMessageDialog(null, main, "Task Window", JOptionPane.PLAIN_MESSAGE);
      }
    }, ModalityState.any()
    );
  }
}
