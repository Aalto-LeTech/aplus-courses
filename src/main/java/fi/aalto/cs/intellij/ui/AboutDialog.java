package fi.aalto.cs.intellij.ui;

import javax.swing.JOptionPane;

public class AboutDialog {
  private static String description = "This plugin supports the educational use of IntelliJ (and "
      + "its Scala plugin) in\nprogramming courses that rely on the A+ course platform, which has "
      + "been\ndeveloped at Aalto University. The plugin accesses programming assignments\nand "
      + "automated grading services provided by A+ and otherwise enhances\nthe student experience.";

  /**
   * Displays a small pop up window containing basic information about the plugin.
   */
  public static void display() {
    JOptionPane.showMessageDialog(null, description, "About A+ Plugin",
        JOptionPane.INFORMATION_MESSAGE);
  }
}
