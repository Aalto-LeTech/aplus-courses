package fi.aalto.cs.apluscourses.ui.exercise;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import fi.aalto.cs.apluscourses.presentation.ModuleSelectionViewModel;
import fi.aalto.cs.apluscourses.ui.ModuleComboBoxListRenderer;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModuleSelectionDialog extends DialogWrapper {
  private ModuleSelectionViewModel viewModel;
  private JPanel basePanel;
  private JComboBox<String> modulesComboBox;

  /**
   * Construct a module selection dialog with the given view model.
   */
  public ModuleSelectionDialog(@NotNull ModuleSelectionViewModel viewModel) {
    super(viewModel.getProject());
    this.viewModel = viewModel;
    setButtonsAlignment(SwingConstants.CENTER);
    setTitle("Select Module");
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
    return new Action[] {getOKAction(), getCancelAction()};
  }

  @Override
  protected void doOKAction() {
    viewModel.setSelectedModule((String) modulesComboBox.getSelectedItem());
    super.doOKAction();
  }

  @Nullable
  @Override
  protected ValidationInfo doValidate() {
    String selectedModuleName = (String) modulesComboBox.getSelectedItem();
    if ("Select module...".equals(selectedModuleName)) {
      return new ValidationInfo("Select a module", modulesComboBox);
    }
    return null;
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    List<String> moduleNames = viewModel.getAvailableModuleNames();
    moduleNames.add(0, "Select module...");
    modulesComboBox = new ComboBox<>(moduleNames.stream().toArray(String[]::new));
    modulesComboBox.setRenderer(new ModuleComboBoxListRenderer());
  }

  @FunctionalInterface
  public interface Factory {
    ModuleSelectionDialog createDialog(@NotNull ModuleSelectionViewModel viewModel);
  }
}
