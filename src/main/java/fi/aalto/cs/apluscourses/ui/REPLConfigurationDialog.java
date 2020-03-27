package fi.aalto.cs.apluscourses.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import fi.aalto.cs.apluscourses.ui.base.DialogBaseHelper;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class REPLConfigurationDialog extends DialogBaseHelper {

  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private REPLConfigurationForm replConfigurationForm;
  private JPanel form;

  public REPLConfigurationDialog() {
    setContentPane(contentPane);
    setModal(true);
    setResizable(false);
    getRootPane().setDefaultButton(buttonOK);
    addDefaultListeners(buttonOK, buttonCancel, contentPane);
    setTitle("REPL Configuration");

    //  location "center" (it's still a big question "center" of what
    this.setLocationRelativeTo(null);
    this.pack();
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

  @Override
  protected void onOK() {
    replConfigurationForm.updateModel();
    super.onOK();
  }
}

