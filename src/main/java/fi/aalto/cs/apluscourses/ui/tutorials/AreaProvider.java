package fi.aalto.cs.apluscourses.ui.tutorials;

import java.awt.Component;
import java.awt.geom.Area;
import org.jetbrains.annotations.NotNull;

public interface AreaProvider {
  @NotNull Area getArea(@NotNull Component destination);
}
