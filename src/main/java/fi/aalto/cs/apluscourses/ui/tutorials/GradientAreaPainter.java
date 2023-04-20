package fi.aalto.cs.apluscourses.ui.tutorials;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import org.jetbrains.annotations.NotNull;

public class GradientAreaPainter extends BasicAreaPainter {

  public GradientAreaPainter(@NotNull Color color, float alpha, @NotNull Runnable animationCallback) {
    super(color, alpha, animationCallback);
  }

  @Override
  protected void prepareGraphics(@NotNull Graphics2D g) {
    super.prepareGraphics(g);

    float y = patternY().get();
    g.setPaint(new GradientPaint(300 * y, 1000 * y, color.darker(), 300 * (.5f + y), 1000 * (.5f + y), color.brighter(), true));
  }
}
