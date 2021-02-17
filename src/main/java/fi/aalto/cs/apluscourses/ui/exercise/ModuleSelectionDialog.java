package fi.aalto.cs.apluscourses.ui.exercise;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import fi.aalto.cs.apluscourses.presentation.ModuleSelectionViewModel;
import fi.aalto.cs.apluscourses.ui.Dialog;
import fi.aalto.cs.apluscourses.ui.GuiObject;
import fi.aalto.cs.apluscourses.ui.IconListCellRenderer;
import fi.aalto.cs.apluscourses.ui.base.OurComboBox;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

public class ModuleSelectionDialog extends DialogWrapper
    implements Dialog {

  private final ModuleSelectionViewModel viewModel;
  private JPanel basePanel;
  private OurComboBox<Module> modulesComboBox;
  @GuiObject
  private JLabel infoLabel;

  /**
   * Construct a module selection dialog with the given view model.
   */
  public ModuleSelectionDialog(@NotNull ModuleSelectionViewModel viewModel,
                               @Nullable Project project) {
    super(project);
    this.viewModel = viewModel;
    setButtonsAlignment(SwingConstants.CENTER);
    setTitle(getText("ui.toolWindow.subTab.exercises.submission.selectModuleShort"));
    init();
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

  @Nullable
  @Override
  protected ValidationInfo doValidate() {
    if (modulesComboBox.getSelectedItem() == null) {
      return new ValidationInfo(
          getText("ui.toolWindow.subTab.exercises.submission.selectAModule"),
          modulesComboBox);
    }
    return null;
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    infoLabel = new JLabel(viewModel.getInfoText());
    modulesComboBox = new OurComboBox<>(viewModel.getModules(), Module.class);
    modulesComboBox.setRenderer(new IconListCellRenderer<>(viewModel.getPrompt(),
        Module::getName, PluginIcons.A_PLUS_MODULE));
    modulesComboBox.selectedItemBindable.bindToSource(viewModel.selectedModule);
  }
}
