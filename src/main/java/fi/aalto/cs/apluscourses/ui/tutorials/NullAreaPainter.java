package fi.aalto.cs.apluscourses.ui.tutorials;

import java.awt.Graphics;
import java.awt.geom.Area;
import org.jetbrains.annotations.NotNull;

public class NullAreaPainter implements AreaPainter {

  private final @NotNull AnimatedValue dumbValue = new DumbAnimatedValue();

  @Override
  public @NotNull AnimatedValue opacity() {
    return dumbValue;
  }

  @Override
  public @NotNull AnimatedValue patternY() { return dumbValue; }

  @Override
  public void paint(@NotNull Graphics graphics, @NotNull Area area) {
    // do nothing
  }
}
