package fi.aalto.cs.apluscourses.ui;

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
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class REPLConfigDialog extends JDialog {

  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private TextFieldWithBrowseButton workingDirectoryField;
  private JCheckBox dontShowThisWindowCheckBox;
  private static boolean showREPLConfig = true;
  private ComboBox moduleComboBox;

  public REPLConfigDialog(Project project, String workDirPath, String targetModuleName) {
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);
    setTitle("REPL Configuration");

    addFileChooser("Choose Working Directory", workingDirectoryField, project);
    workingDirectoryField.setText(workDirPath);

    Arrays.stream(ModuleManager.getInstance(project).getModules())
        .filter(module -> module.getModuleTypeName().equals("JAVA_MODULE"))
        .map(Module::getName)
        .forEach(moduleName -> moduleComboBox.addItem(moduleName));
    moduleComboBox.setSelectedItem(targetModuleName);
    moduleComboBox.setEnabled(showREPLConfig);
    moduleComboBox.setRenderer(new ModuleComboBoxListRenderer());

    dontShowThisWindowCheckBox.setSelected(false);
    dontShowThisWindowCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showREPLConfig = !showREPLConfig;
      }
    });

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

  private void onOK() {
    // add your code here
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

  public TextFieldWithBrowseButton getWorkingDirectoryField() {
    return workingDirectoryField;
  }

  public void setWorkingDirectoryField(
      TextFieldWithBrowseButton workingDirectoryField) {
    this.workingDirectoryField = workingDirectoryField;
  }

  public JCheckBox getDontShowThisWindowCheckBox() {
    return dontShowThisWindowCheckBox;
  }

  public void setDontShowThisWindowCheckBox(JCheckBox dontShowThisWindowCheckBox) {
    this.dontShowThisWindowCheckBox = dontShowThisWindowCheckBox;
  }

  public ComboBox getModuleComboBox() {
    return moduleComboBox;
  }

  public void setModuleComboBox(ComboBox moduleComboBox) {
    this.moduleComboBox = moduleComboBox;
  }

  public static boolean showREPLConfig() {
    return showREPLConfig;
  }

  public static void setShowREPLConfig(boolean showREPLConfig) {
    REPLConfigDialog.showREPLConfig = showREPLConfig;
  }

  {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
    $$$setupUI$$$();
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT edit this method OR
   * call it in your code!
   *
   * @noinspection ALL
   */
  private void $$$setupUI$$$() {
    contentPane = new JPanel();
    contentPane.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
    contentPane.setMinimumSize(new Dimension(278, 260));
    contentPane.setName("Test");
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridLayoutManager(6, 1, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel1,
        new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null,
            null, 0, false));
    final JLabel label1 = new JLabel();
    label1.setText("Working directory:");
    panel1.add(label1,
        new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null,
            new Dimension(595, 21), null, 0, false));
    workingDirectoryField = new TextFieldWithBrowseButton();
    panel1.add(workingDirectoryField, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
        GridConstraints.FILL_HORIZONTAL,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null,
        new Dimension(595, 38), null, 0, false));
    final JLabel label2 = new JLabel();
    label2.setText("Use classpath and SDK of module:");
    panel1.add(label2,
        new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null,
            new Dimension(595, 21), null, 0, false));
    final Spacer spacer1 = new Spacer();
    panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
        GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null,
        new Dimension(595, 14), null, 0, false));
    dontShowThisWindowCheckBox = new JCheckBox();
    dontShowThisWindowCheckBox.setText("Don't show this window again");
    panel1.add(dontShowThisWindowCheckBox,
        new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(595, 23), null, 0, false));
    moduleComboBox = new ComboBox();
    final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
    moduleComboBox.setModel(defaultComboBoxModel1);
    panel1.add(moduleComboBox, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST,
        GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW,
        GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(595, 38), null, 0, false));
  }

  /**
   * @noinspection ALL
   */
  public JComponent $$$getRootComponent$$$() {
    return contentPane;
  }
}
