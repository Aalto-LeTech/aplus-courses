package fi.aalto.cs.apluscourses.ui.courseproject;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import fi.aalto.cs.apluscourses.presentation.CourseProjectViewModel;
import fi.aalto.cs.apluscourses.ui.GuiObject;
import fi.aalto.cs.apluscourses.ui.base.CheckBox;
import fi.aalto.cs.apluscourses.ui.base.TemplateLabel;
import fi.aalto.cs.apluscourses.utils.PluginResourceBundle;
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
  private CheckBox settingsOptOutCheckbox;
  @GuiObject
  private JLabel warningText;

  CourseProjectView(@NotNull Project project,
                    @NotNull CourseProjectViewModel viewModel) {
    super(project);

    setTitle(PluginResourceBundle.getText("ui.courseProject.view"));
    setButtonsAlignment(SwingConstants.CENTER);

    init();

    infoText.setIcon(Messages.getInformationIcon());

    settingsOptOutCheckbox.isCheckedBindable.bindToSource(viewModel.settingsOptOutProperty);

    settingsOptOutCheckbox.setEnabled(viewModel.canUserOptOutSettings());

    warningText.setVisible(viewModel.shouldWarnUser());
    settingsInfoText.setVisible(viewModel.shouldShowSettingsInfo());

    infoText.applyTemplate(viewModel.getCourseName());

    currentSettingsText.applyTemplate(viewModel.getCourseName());
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
