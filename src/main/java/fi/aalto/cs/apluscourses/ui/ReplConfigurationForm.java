package fi.aalto.cs.apluscourses.ui;

import static fi.aalto.cs.apluscourses.presentation.ReplConfigurationModel.showREPLConfigWindow;
import static java.util.Objects.requireNonNull;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import fi.aalto.cs.apluscourses.presentation.ReplConfigurationModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ReplConfigurationForm extends JPanel {

  private ReplConfigurationModel model;

  private TextFieldWithBrowseButton workingDirectoryField;
  private ComboBox moduleComboBox;
  private JCheckBox dontShowThisWindowCheckBox;
  private JPanel contentPane;
  private JLabel infoText;

  public ReplConfigurationForm() {
  }

  public ReplConfigurationForm(ReplConfigurationModel model) {
    this.model = model;
    dontShowThisWindowCheckBox.setSelected(!showREPLConfigWindow);
    dontShowThisWindowCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showREPLConfigWindow = !showREPLConfigWindow;
      }
    });

    infoText.setText("<html>"
        + "This is an A+ Scala REPL configuration window. By default, the working "
        + "directory and the dependencies, loaded to the REPL classpath belong to the Module, that "
        + "it was started on. To change the behavior, use the checkbox at the bottom of the window."
        + "</html>");

    addFileChooser("Choose Working Directory", workingDirectoryField, model.getProject());
    workingDirectoryField.setText(model.getWorkingDirectory());

    model.getModules().forEach(moduleName -> moduleComboBox.addItem(moduleName));
    moduleComboBox.setSelectedItem(model.getTargetModuleName());
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

  public JPanel getContentPane() {
    return contentPane;
  }

  public void setContentPane(JPanel contentPane) {
    this.contentPane = contentPane;
  }

  protected void updateModel() {
    model.setTargetModuleName(requireNonNull(moduleComboBox.getSelectedItem()).toString());
    model.setWorkingDirectory(workingDirectoryField.getText());
  }

  public ReplConfigurationModel getModel() {
    return model;
  }

  public void setModel(ReplConfigurationModel model) {
    this.model = model;
  }
}
