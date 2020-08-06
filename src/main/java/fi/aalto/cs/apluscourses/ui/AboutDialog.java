package fi.aalto.cs.apluscourses.ui;

import fi.aalto.cs.apluscourses.utils.PluginResourceBundle;
import javax.swing.JOptionPane;

public class AboutDialog {

  private AboutDialog() {

  }

  /**
   * Displays a small pop up window containing basic information about the plugin.
   */
  public static void display() {
    JOptionPane.showMessageDialog(null,
        PluginResourceBundle.getText("ui.aboutDialog.description"),
        PluginResourceBundle.getText("ui.aboutDialog.title"),
        JOptionPane.INFORMATION_MESSAGE);
  }
}
