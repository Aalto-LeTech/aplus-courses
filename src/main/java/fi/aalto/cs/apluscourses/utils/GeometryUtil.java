package fi.aalto.cs.apluscourses.utils;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import org.jetbrains.annotations.NotNull;

public class GeometryUtil {

  private GeometryUtil() {

  }

  public static @NotNull Shape withMargin(@NotNull Rectangle r, double margin) {
    return new RoundRectangle2D.Double(
        r.x - margin,
        r.y - margin,
        r.width + margin * 2,
        r.height + margin * 2, margin,
        margin
    );
  }
}
