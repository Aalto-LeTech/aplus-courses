package fi.aalto.cs.apluscourses.ui.repl;

import com.intellij.ui.SimpleListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import org.jetbrains.annotations.NotNull;

/**
 * Custom renderer for items (modules in this case) stored in {@link javax.swing.JComboBox}
 */
public class ModuleComboBoxListRenderer extends SimpleListCellRenderer<String> {

  //todo add the icon path when developing locally
  public static final String iconPath = "/u/39/denissn2/unix/IdeaProjects/intellij-plugin"
      + "/src/main/resources/META-INF/icons/module.png";

  @Override
  public void customize(@NotNull JList<? extends String> list, String value, int index,
      boolean selected, boolean hasFocus) {
    ImageIcon icon = new ImageIcon(iconPath);
    setText(value);
    setIcon(icon);
  }
}
