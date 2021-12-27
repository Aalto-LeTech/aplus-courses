package fi.aalto.cs.apluscourses.ui.toolwindowcards;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.activities.InitializationActivity;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public class NetworkErrorCard {
  private JButton retryConnectionButton;
  private JPanel panel;

  /**
   * Constructor.
   */
  public NetworkErrorCard(@NotNull Project project) {
    retryConnectionButton.addActionListener(e -> {
      var mainVm = PluginSettings.getInstance().getMainViewModel(project);
      var cardVm = mainVm.toolWindowCardViewModel;
      cardVm.setNetworkError(false);
      new InitializationActivity().runActivity(project);
    });
  }

  public JPanel getPanel() {
    return panel;
  }
}
