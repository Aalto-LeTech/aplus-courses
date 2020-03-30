package fi.aalto.cs.apluscourses.ui.repl;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Custom renderer for items (modules in this case) stored in {@link javax.swing.JComboBox}
 */
public class ModuleComboBoxListRenderer extends JLabel implements ListCellRenderer {

  //todo add the icon path when developing locally
  public static final String iconPath = "/u/39/denissn2/unix/IdeaProjects/intellij-plugin"
      + "/src/main/resources/META-INF/icons/module.png";

  @Override
  public Component getListCellRendererComponent(
      JList list,
      Object value,
      int index,
      boolean isSelected,
      boolean cellHasFocus) {

    ImageIcon icon = new ImageIcon(iconPath);
    setText(value.toString());
    setIcon(icon);

    return this;
  }
}
