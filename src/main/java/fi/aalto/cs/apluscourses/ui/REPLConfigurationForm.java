package fi.aalto.cs.apluscourses.ui;

import static fi.aalto.cs.apluscourses.ui.REPLConfigurationDialog.showREPLConfigWindow;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class REPLConfigurationForm extends JPanel {

  private TextFieldWithBrowseButton workingDirectoryField;
  private ComboBox moduleComboBox;
  private JCheckBox dontShowThisWindowCheckBox;
  private JPanel contentPane;

  public REPLConfigurationForm() {
  }

  public REPLConfigurationForm(Project project, String workDirPath, String targetModuleName) {
    dontShowThisWindowCheckBox.setSelected(false);
    dontShowThisWindowCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showREPLConfigWindow = !showREPLConfigWindow;
      }
    });

    addFileChooser("Choose Working Directory", workingDirectoryField, project);
    workingDirectoryField.setText(workDirPath);

    Arrays.stream(ModuleManager.getInstance(project).getModules())
        .filter(module -> module.getModuleTypeName().equals("JAVA_MODULE"))
        .map(Module::getName)
        .forEach(moduleName -> moduleComboBox.addItem(moduleName));
    moduleComboBox.setSelectedItem(targetModuleName);
    moduleComboBox.setEnabled(showREPLConfigWindow);
    moduleComboBox.setRenderer(new ModuleComboBoxListRenderer());
  }

  private void addFileChooser(final String title,
      final TextFieldWithBrowseButton textField,
      final Project project) {
    final FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(false, true,
        false, false, false, false) {
      @Override
      public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
        return super.isFileVisible(file, showHiddenFiles) && file.isDirectory();
      }
    };
    fileChooserDescriptor.setTitle(title);
    textField.addBrowseFolderListener(title, null, project, fileChooserDescriptor);
  }

  public TextFieldWithBrowseButton getWorkingDirectoryField() {
    return workingDirectoryField;
  }

  public void setWorkingDirectoryField(
      TextFieldWithBrowseButton workingDirectoryField) {
    this.workingDirectoryField = workingDirectoryField;
  }

  public ComboBox getModuleComboBox() {
    return moduleComboBox;
  }

  public void setModuleComboBox(ComboBox moduleComboBox) {
    this.moduleComboBox = moduleComboBox;
  }

  public JCheckBox getDontShowThisWindowCheckBox() {
    return dontShowThisWindowCheckBox;
  }

  public void setDontShowThisWindowCheckBox(JCheckBox dontShowThisWindowCheckBox) {
    this.dontShowThisWindowCheckBox = dontShowThisWindowCheckBox;
  }

  public JPanel getContentPane() {
    return contentPane;
  }

  public void setContentPane(JPanel contentPane) {
    this.contentPane = contentPane;
  }
}
