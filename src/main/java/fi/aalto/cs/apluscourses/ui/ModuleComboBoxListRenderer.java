package fi.aalto.cs.apluscourses.ui;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ModuleComboBoxListRenderer extends JLabel implements ListCellRenderer {

  @Override
  public Component getListCellRendererComponent(JList list,
      Object value,
      int index,
      boolean isSelected,
      boolean cellHasFocus) {

    //todo add the icon when developing locally
    ImageIcon icon4 = new ImageIcon(
        "/u/39/denissn2/unix/IdeaProjects/intellij-plugin/src/main/resources/META-INF/icons/module.png");
    setText(value.toString());
    setIcon(icon4);

    return this;
  }
}
