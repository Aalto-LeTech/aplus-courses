package fi.aalto.cs.apluscourses.ui.tutorials;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import org.jetbrains.annotations.NotNull;

public class BasicAreaPainter implements AreaPainter {
  protected final @NotNull Color color;
  protected final float alpha;

  private final @NotNull AnimatedValue mOpacity;

  private final @NotNull AnimatedValue mPatternY;

  public BasicAreaPainter(@NotNull Color color, float alpha, @NotNull Runnable animationCallback) {
    this.color = color;
    this.alpha = alpha;
    mOpacity = new AnimatedValueImpl(50, animationCallback);
    mPatternY = new AnimatedValueImpl(50, animationCallback);
  }

  @Override
  public @NotNull AnimatedValue opacity() {
    return mOpacity;
  }

  @Override
  public @NotNull AnimatedValue patternY() {
    return mPatternY;
  }

  @Override
  public void paint(@NotNull Graphics graphics, @NotNull Area area) {
    if (area.isEmpty()) {
      return;
    }

    Graphics2D g = (Graphics2D) graphics.create();

    prepareGraphics(g);

    g.fill(area);
    g.dispose();
  }

  protected void prepareGraphics(@NotNull Graphics2D g) {
    g.setComposite(AlphaComposite.SrcOver.derive(alpha * opacity().get()));
    g.setColor(color);

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
  }
}
