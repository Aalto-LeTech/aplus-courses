package fi.aalto.cs.apluscourses.ui.courseproject;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import fi.aalto.cs.apluscourses.presentation.CourseProjectViewModel;
import fi.aalto.cs.apluscourses.ui.Binding;
import fi.aalto.cs.apluscourses.ui.CheckBox;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CourseProjectView extends DialogWrapper {
  private CourseProjectViewModel courseProjectViewModel;
  private JPanel basePanel;
  @Binding
  private JLabel infoText;
  @Binding
  private JLabel settingsText;
  @Binding
  private JCheckBox restartCheckbox;
  @Binding
  private JCheckBox settingsOptOutCheckbox;

  CourseProjectView(@NotNull Project project,
                    @NotNull CourseProjectViewModel courseProjectViewModel) {
    super(project);
    this.courseProjectViewModel = courseProjectViewModel;
    setTitle("Turn Project Into A+ Course Project");
    setButtonsAlignment(SwingConstants.CENTER);
    init();
  }

  @Override
  public void doCancelAction() {
    courseProjectViewModel.userCancels.set(true);
    close(CANCEL_EXIT_CODE);
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
    infoText.setText(courseProjectViewModel.getInformationText());

    settingsText = new JLabel(courseProjectViewModel.getSettingsText());

    restartCheckbox = new CheckBox(
        courseProjectViewModel.getRestartCheckboxText(),
        courseProjectViewModel.userWantsRestart,
        courseProjectViewModel.isRestartAvailable);

    settingsOptOutCheckbox = new CheckBox(
        courseProjectViewModel.getOptOutCheckboxText(),
        courseProjectViewModel.userOptsOutOfSettings,
        courseProjectViewModel.isSettingsOptOutAvailable);
  }
}
