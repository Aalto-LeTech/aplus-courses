package fi.aalto.cs.apluscourses.ui.tutorials;

import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.geom.Area;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Piece {
  @NotNull Area getArea(@NotNull Component destination);

  boolean hasFocus();

  boolean contains(@Nullable Point point);

  default boolean isActive() {
    return hasFocus() || contains(MouseInfo.getPointerInfo().getLocation());
  }
}
