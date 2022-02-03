package fi.aalto.cs.apluscourses.ui.ideactivities;

import com.intellij.util.ui.JBValue;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * A generic highlighter class, used by {@link OverlayPane} to highlight an entire component.
 */
public class GenericHighlighter {

  private final @NotNull Component component;

  public @NotNull Component getComponent() {
    return component;
  }

  /**
   * Gets the area as RoundRectangles2D, with the arc length corresponding
   * to the current theme's default arc length.
   */
  public List<RectangularShape> getArea() {
    final float arc = new JBValue.UIInteger("Button.arc", 6).getFloat();
    return Collections.singletonList(
        new RoundRectangle2D.Float(
            0, 0, getComponent().getWidth(), getComponent().getHeight(), arc, arc));
  }

  public GenericHighlighter(@NotNull Component component) {
    this.component = component;
  }
}
