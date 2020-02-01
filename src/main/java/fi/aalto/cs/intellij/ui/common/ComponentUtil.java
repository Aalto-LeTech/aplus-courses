package fi.aalto.cs.intellij.ui.common;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Collections;
import java.util.Map;
import javax.swing.JComponent;

public class ComponentUtil {
  public static void setFont(JComponent component, Map<TextAttribute, Object> attributes) {
    Font font = component.getFont();
    if (font != null) {
      component.setFont(font.deriveFont(attributes));
    }
  }

  public static void setFont(JComponent component, TextAttribute attribute, Object value) {
    setFont(component, Collections.singletonMap(attribute, value));
  }

  private ComponentUtil() {

  }
}
