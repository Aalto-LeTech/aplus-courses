package fi.aalto.cs.apluscourses.ui.ideactivities;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.ui.components.JBLabel;import fi.aalto.cs.apluscourses.presentation.ideactivities.TaskViewModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

public class TaskView {

  private JPanel main;
  private JLabel label;

  public TaskView() {
    main = new JPanel();
    label = new JBLabel();
    main.add(label);
  }

  public void viewModelChanged(@Nullable TaskViewModel viewModel) {
    ApplicationManager.getApplication().invokeLater(() -> {
      if (viewModel != null) {
        label.setText("<html>" + viewModel.getAction()+ "<br>" + viewModel.getCurrentTask().getFile() + "</html>");
        JOptionPane.showMessageDialog(null,
        main,
        "Task Window",
        JOptionPane.PLAIN_MESSAGE);
      }
    }, ModalityState.any()
    );
  }
}
