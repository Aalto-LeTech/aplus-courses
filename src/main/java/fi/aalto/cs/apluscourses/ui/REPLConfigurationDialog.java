package fi.aalto.cs.apluscourses.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class REPLConfigurationDialog extends JDialog {

  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private REPLConfigurationForm replConfigurationForm;
  private JPanel form;
  protected static boolean showREPLConfigWindow = true;

  public REPLConfigurationDialog(REPLConfigurationForm replConfigurationForm) {
    this();
    setReplConfigurationForm(replConfigurationForm);
  }

  public REPLConfigurationDialog() {
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);
    setTitle("REPL Configuration");

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
    // add your code here i f necessary
    dispose();
  }

  public REPLConfigurationForm getReplConfigurationForm() {
    return replConfigurationForm;
  }

  public void setReplConfigurationForm(
      REPLConfigurationForm replConfigurationForm) {
    this.replConfigurationForm = replConfigurationForm;
    replaceReplConfigurationFormWithIn(replConfigurationForm, form);
  }

  private void replaceReplConfigurationFormWithIn(REPLConfigurationForm replConfigurationForm,
      JPanel form) {
    final GridConstraints gridConstraints = new GridConstraints(0, 0, 1, 1,
        GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null,
        null, 0, false);

    //the only one in this particular implementation
    if (form.getComponent(0) != null) {
      form.remove(0);
    }
    form.add(replConfigurationForm.getContentPane(),
        gridConstraints);
    form.revalidate();
    form.repaint();
  }
}
