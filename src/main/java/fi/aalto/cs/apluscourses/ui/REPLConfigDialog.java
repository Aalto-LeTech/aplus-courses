package fi.aalto.cs.apluscourses.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;

public class REPLConfigDialog extends JDialog {

  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private TextFieldWithBrowseButton workingDirectoryField;
  private JCheckBox dontShowThisWindowCheckBox;
  private ComboBox moduleComboBox;

  public REPLConfigDialog(Project project, Module targetModule) {
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);
    setTitle("REPL Configuration");

    addFileChooser("Choose Working Directory", workingDirectoryField, project);
    String workDir = ModuleUtilCore.getModuleDirPath(targetModule);
    String path = getPath(project, workDir);
    workingDirectoryField.setText(path);

    getJavaModuleNames(project).forEach(name -> moduleComboBox.addItem(name));
    moduleComboBox.setSelectedItem(targetModule.getName());
    moduleComboBox.setEnabled(true);
    moduleComboBox.setRenderer(new ModuleComboBoxListRenderer());

    dontShowThisWindowCheckBox.setSelected(true);
    System.out.println(moduleComboBox.getSelectedItem());

//  location "center" (it's still a big question "center" of what
    this.setLocationRelativeTo(null);
    this.pack();

    buttonOK.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onOK();
      }
    });

    buttonCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onCancel();
      }
    });

    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onCancel();
      }
    });

    // call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(new ActionListener() {
                                         public void actionPerformed(ActionEvent e) {
                                           onCancel();
                                         }
                                       }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  private String getPath(Project project, String workDir) {
    String directory = !workDir.isEmpty() ? workDir : project.getBaseDir().getPath();
    return directory.replace("/.idea", "");
  }

  @NotNull
  private List<String> getJavaModuleNames(Project project) {
    return Arrays.stream(ModuleManager.getInstance(project).getModules())
        .filter(module -> module.getModuleTypeName().equals("JAVA_MODULE"))
        .map(Module::getName)
        .collect(Collectors.toList());
  }

  private void onOK() {
    // add your code here
    System.out.println("selected: " + moduleComboBox.getSelectedItem());
    dispose();
  }

  private void onCancel() {
    // add your code here if necessary
    dispose();
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
}
