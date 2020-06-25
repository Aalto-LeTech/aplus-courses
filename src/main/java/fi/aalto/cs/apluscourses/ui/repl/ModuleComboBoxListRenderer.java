package fi.aalto.cs.apluscourses.ui.repl;

import com.intellij.ui.SimpleListCellRenderer;
import icons.PluginIcons;
import javax.swing.JList;
import org.jetbrains.annotations.NotNull;

/**
 * Custom renderer for items (modules in this case) stored in {@link javax.swing.JComboBox}.
 */
public class ModuleComboBoxListRenderer extends SimpleListCellRenderer<String> {

  @Override
  public void customize(@NotNull JList<? extends String> list, String value, int index,
      boolean selected, boolean hasFocus) {
    setText(value);
    setIcon(PluginIcons.A_PLUS_MODULE);
  }
}
