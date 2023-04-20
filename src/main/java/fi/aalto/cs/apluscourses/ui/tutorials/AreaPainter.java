package fi.aalto.cs.apluscourses.ui.tutorials;

import java.awt.Graphics;
import java.awt.geom.Area;
import org.jetbrains.annotations.NotNull;

public interface AreaPainter {
  @NotNull AnimatedValue opacity();

  @NotNull AnimatedValue patternY();

  void paint(@NotNull Graphics graphics, @NotNull Area area);
}
