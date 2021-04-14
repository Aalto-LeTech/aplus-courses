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
  private JLabel label;
  private final TaskViewModel viewModel;


  public TaskView(@NotNull TaskViewModel viewModel) {
    main = new JPanel();
    label = new JBLabel();
    main.add(label);
    this.viewModel = viewModel;
  }

  //Also, send termination signal in order to cancel Tutorial and free up resources!
  public static void createAndShow(@NotNull TaskViewModel taskViewModel) {
    new TaskView(taskViewModel).show();
  }

  public void show() {
    ApplicationManager.getApplication().invokeLater(() -> {
      if (viewModel != null) {
        label.setText("<html>" + viewModel.getAction() + "<br>"
                + viewModel.getTask().getFile() + "</html>");
        JOptionPane.showMessageDialog(null, main, "Task Window", JOptionPane.PLAIN_MESSAGE);
      }
    }, ModalityState.any()
    );
  }
}
