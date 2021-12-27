package fi.aalto.cs.apluscourses.ui.toolwindowcards;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.actions.APlusAuthenticationAction;
import fi.aalto.cs.apluscourses.intellij.actions.ActionUtil;
import fi.aalto.cs.apluscourses.ui.module.ModulesDialog;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public class NoTokenCard {
  private JButton setATokenButton;
  private JButton modulesButton;
  private JPanel panel;

  /**
   * Constructor.
   */
  public NoTokenCard(@NotNull Project project) {
    setATokenButton.addActionListener(e -> {
      DataContext context = DataManager.getInstance().getDataContext(setATokenButton);
      ActionUtil.launch(APlusAuthenticationAction.ACTION_ID, context);
    });
    modulesButton.addActionListener(e -> {
      var modulesDialog = new ModulesDialog(project);
      modulesDialog.show();
    });
  }

  public JPanel getPanel() {
    return panel;
  }
}
