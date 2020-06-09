package fi.aalto.cs.apluscourses.ui.courseproject;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import fi.aalto.cs.apluscourses.presentation.CourseProjectViewModel;
import fi.aalto.cs.apluscourses.ui.GuiObject;
import fi.aalto.cs.apluscourses.ui.CheckBox;
import fi.aalto.cs.apluscourses.ui.TemplateLabel;
import fi.aalto.cs.apluscourses.utils.bindable.Bindable;
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

  private final Bindable<JLabel, Boolean> isWarningTextVisibleBindable =
      new Bindable<>(warningText, JLabel::setVisible);
  private final Bindable<JLabel, Boolean> isSettingsInfoTextVisibleBindable =
      new Bindable<>(settingsInfoText, JLabel::setVisible);

  CourseProjectView(@NotNull Project project,
                    @NotNull CourseProjectViewModel viewModel) {
    super(project);
    init();

    setTitle("Turn Project Into A+ Course Project");
    setButtonsAlignment(SwingConstants.CENTER);
    infoText.setIcon(Messages.getInformationIcon());

    restartCheckBox.isCheckedBindable.bindToSource(viewModel.restartProperty);
    restartCheckBox.isEnabledBindable.bindToSource(viewModel.isRestartAvailableProperty);

    settingsOptOutCheckbox.isCheckedBindable.bindToSource(viewModel.settingsOptOutProperty);
    settingsOptOutCheckbox.isEnabledBindable.bindToSource(viewModel.canUserOptOutSettings());

    isWarningTextVisibleBindable.bindToSource(viewModel.shouldWarnUser());

    isSettingsInfoTextVisibleBindable.bindToSource(viewModel.shouldShowSettingsInfo());

    infoText.templateArgumentBindable.bindToSource(viewModel.getCourseName());

    currentSettingsText.templateArgumentBindable.bindToSource(viewModel.getCurrentSettings());
    currentSettingsText.isVisibleBindable.bindToSource(viewModel.shouldShowCurrentSettings());
  }

  @Override
  public void doCancelAction() {
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
    return new Action[] { getOKAction(), getCancelAction() };
  }
}
