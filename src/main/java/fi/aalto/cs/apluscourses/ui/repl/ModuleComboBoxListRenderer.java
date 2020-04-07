package fi.aalto.cs.apluscourses.ui.repl;

import com.intellij.ui.SimpleListCellRenderer;
import fi.aalto.cs.apluscourses.utils.ResourceException;
import fi.aalto.cs.apluscourses.utils.Resources;
import javax.swing.ImageIcon;
import javax.swing.JList;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom renderer for items (modules in this case) stored in {@link javax.swing.JComboBox}.
 */
public class ModuleComboBoxListRenderer extends SimpleListCellRenderer<String> {

  private static final Logger logger = LoggerFactory.getLogger(ModuleComboBoxListRenderer.class);

  public static String iconPath = "META-INF/icons/module.png";

  @Override
  public void customize(@NotNull JList<? extends String> list, String value, int index,
      boolean selected, boolean hasFocus) {
    ImageIcon icon = null;
    try {
      icon = new ImageIcon(Resources.DEFAULT.getImage(iconPath));
    } catch (ResourceException ex) {
      logger.error("Could not load the icon resource from the path", ex);
    }
    setText(value);
    setIcon(icon);
  }
}
