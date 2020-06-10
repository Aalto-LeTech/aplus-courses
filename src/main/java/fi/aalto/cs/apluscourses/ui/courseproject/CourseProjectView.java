package fi.aalto.cs.apluscourses.ui.courseproject;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import fi.aalto.cs.apluscourses.presentation.CourseProjectViewModel;
import fi.aalto.cs.apluscourses.ui.CheckBox;
import fi.aalto.cs.apluscourses.ui.GuiObject;
import fi.aalto.cs.apluscourses.ui.TemplateLabel;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CourseProjectView extends DialogWrapper {
  private JPanel basePanel;
  @GuiObject
  private TemplateLabel infoText;
  @GuiObject
  private TemplateLabel currentSettingsText;
  @GuiObject
  private JLabel settingsInfoText;
  @GuiObject
  private CheckBox restartCheckBox;
  @GuiObject
  private CheckBox settingsOptOutCheckbox;
  @GuiObject
  private JLabel warningText;

  CourseProjectView(@NotNull Project project,
                    @NotNull CourseProjectViewModel viewModel) {
    super(project);

    setTitle("Turn Project Into A+ Course Project");
    setButtonsAlignment(SwingConstants.CENTER);

    init();

    infoText.setIcon(Messages.getInformationIcon());

    restartCheckBox.isCheckedBindable.bindToSource(viewModel.restartProperty);
    restartCheckBox.isEnabledBindable.bindToSource(viewModel.isRestartAvailableProperty);

    settingsOptOutCheckbox.isCheckedBindable.bindToSource(viewModel.settingsOptOutProperty);

    settingsOptOutCheckbox.setEnabled(viewModel.canUserOptOutSettings());

    warningText.setVisible(viewModel.shouldWarnUser());
    settingsInfoText.setVisible(viewModel.shouldShowSettingsInfo());

    infoText.applyTemplate(viewModel.getCourseName());

    currentSettingsText.applyTemplate(viewModel.getCurrentSettings());
    currentSettingsText.setVisible(viewModel.shouldShowCurrentSettings());
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return basePanel;
  }

  @NotNull
  @Override
  protected Action[] createActions() {
    return new Action[] { getOKAction(), getCancelAction() };
  }
}
