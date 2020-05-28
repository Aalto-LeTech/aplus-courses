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
  private String currentlyImportedSettings;
  private JPanel basePanel;
  private JLabel infoText;
  private JLabel settingsText;
  private JCheckBox restartCheckbox;
  private JCheckBox settingsOptOutCheckbox;

  CourseProjectDialog(@NotNull Project project,
                      @NotNull String courseName,
                      @Nullable String currentlyImportedSettings) {
    super(project);
    this.courseName = courseName;
    this.currentlyImportedSettings = currentlyImportedSettings;
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
    infoText = new JLabel(Messages.getInformationIcon());
    infoText.setText("<html><body>The currently opened project will be turned into a project for "
        + "the course <b>" + courseName + "</b>.</body></html>");

    restartCheckbox = new JCheckBox("Restart IntelliJ IDEA to reload settings.", true);

    if (courseName.equals(currentlyImportedSettings)) {
      settingsText = new JLabel("<html><body>IntelliJ IDEA settings are already imported for "
          + "<b>" + courseName + ".</b></body></html>");
      settingsOptOutCheckbox = new JCheckBox("Leave IntelliJ settings unchanged.", true);
      restartCheckbox.setSelected(false);
      restartCheckbox.setEnabled(false);
      settingsOptOutCheckbox.setEnabled(false);
    } else {
      settingsText = new JLabel("The A+ Courses plugin will adjust IntelliJ IDEA settings. "
          + "This helps use IDEA for coursework.");
      settingsOptOutCheckbox = new JCheckBox("<html><body>Leave IntelliJ settings unchanged.<br>"
          + "(<b>Not recommended</b>. Only pick this option if you are sure you know what you are "
          + "doing.)</body></html>", false);
    }
  }
}
