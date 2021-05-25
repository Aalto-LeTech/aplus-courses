package fi.aalto.cs.apluscourses.ui.ideactivities;

import java.awt.Component;
import java.awt.Rectangle;
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

  public List<Rectangle> getArea() {
    return Collections.singletonList(
        new Rectangle(0, 0, component.getWidth(), component.getHeight()));
  }

  public GenericHighlighter(@NotNull Component component) {
    this.component = component;
  }
}
