package fi.aalto.cs.apluscourses.ui.courseproject;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import java.awt.event.ItemEvent;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CourseProjectDialog extends DialogWrapper {
  private String courseName;
  private JPanel basePanel;
  private JLabel textLabel;
  private JCheckBox restartCheckbox;
  private JCheckBox settingsOptOutCheckbox;

  CourseProjectDialog(@NotNull Project project, @NotNull String courseName) {
    // SHOULD WE CHECK HERE IF THE IDE SETTINGS HAVE ALREADY BEEN IMPORTED, AND ONLY IMPORT THEM IF
    // THEY HAVEN'T?
    super(project);
    this.courseName = courseName;
    setTitle("Turn Project Into A+ Course Project");
    setButtonsAlignment(SwingConstants.CENTER);
    init();
    settingsOptOutCheckbox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        restartCheckbox.setSelected(false);
        restartCheckbox.setEnabled(false);
      } else if (e.getStateChange() == ItemEvent.DESELECTED) {
        restartCheckbox.setEnabled(true);
      }
    });
  }

  @Override
  protected void doOKAction() {
    if (settingsOptOutCheckbox.isSelected()) {
      close(CourseProjectActionDialogs.OK_WITH_OPT_OUT);
    } else if (restartCheckbox.isSelected()) {
      close(CourseProjectActionDialogs.OK_WITH_RESTART);
    } else {
      close(CourseProjectActionDialogs.OK_WITHOUT_RESTART);
    }
  }

  @Override
  public void doCancelAction() {
    close(CourseProjectActionDialogs.CANCEL);
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return basePanel;
  }

  @NotNull
  @Override
  protected Action[] createActions() {
    return new Action[]{getOKAction(), getCancelAction()};
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    textLabel = new JLabel(Messages.getInformationIcon());
    textLabel.setText("<html><body>The currently opened project will be turned into a project for "
        + "the course <b>" + courseName + "</b>.</body></html>");
  }
}
