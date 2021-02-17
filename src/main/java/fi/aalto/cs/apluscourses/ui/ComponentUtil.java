package fi.aalto.cs.apluscourses.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.Collections;
import java.util.Map;

/**
 * A helper class for {@link JComponent} objects.
 */
public class ComponentUtil {
  /**
   * Changes a component's font by setting given {@link TextAttribute}s to given values.
   * @param component A {@link JComponent} whose font is changed.
   * @param attributes A {@link Map} of {@link TextAttribute}s and their values to be set.
   */
  public static void setFont(JComponent component, Map<TextAttribute, Object> attributes) {
    Font font = component.getFont();
    if (font != null) {
      component.setFont(font.deriveFont(attributes));
    }
  }

  /**
   * Changes a component's font by setting a given {@link TextAttribute} to a given value.
   * @param component A {@link JComponent} whose font is changed.
   * @param attribute A {@link TextAttribute} to be set.
   * @param value Value for {@code attribute}.
   */
  public static void setFont(JComponent component, TextAttribute attribute, Object value) {
    setFont(component, Collections.singletonMap(attribute, value));
  }

  private ComponentUtil() {

  }
}
