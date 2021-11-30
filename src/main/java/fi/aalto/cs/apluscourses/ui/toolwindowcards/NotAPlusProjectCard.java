package fi.aalto.cs.apluscourses.ui.toolwindowcards;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import fi.aalto.cs.apluscourses.intellij.actions.ActionUtil;
import fi.aalto.cs.apluscourses.intellij.actions.CourseProjectAction;
import javax.swing.JButton;
import javax.swing.JPanel;

public class NotAPlusProjectCard {
  private JButton aplusProjectButton;
  private JPanel panel;

  /**
   * Constructor.
   */
  public NotAPlusProjectCard() {
    aplusProjectButton.addActionListener(e -> {
      DataContext context = DataManager.getInstance().getDataContext(aplusProjectButton);
      ActionUtil.launch(CourseProjectAction.ACTION_ID, context);
    });
  }

  public JPanel getPanel() {
    return panel;
  }
}
