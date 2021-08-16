package fi.aalto.cs.apluscourses.ui.ideactivities;

import com.intellij.util.ui.JBUI;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.border.AbstractBorder;
import org.jetbrains.annotations.NotNull;

/**
 * A border that works like EmptyBorder, except that it also paints a shadow
 * outside of its default padding insets.
 * The padding insets are normally drawn and highlighted. The shadow
 * has a transparent background and is never highlighted.
 */
public class BalloonShadowBorder extends AbstractBorder {
  private final int borderWidth;

  private static final float MAX_SHADOW_ALPHA = 0.8f; // maximum opacity the shadow can reach (max: 1.0f)

  @NotNull
  private final Insets paddingInsets;

  public BalloonShadowBorder(int width, @NotNull Insets paddingInsets) {
    this.paddingInsets = paddingInsets;
    borderWidth = width;
  }

  @Override
  public Insets getBorderInsets(Component c) {
    return JBUI.insets(
        paddingInsets.top + borderWidth,
        paddingInsets.left + borderWidth,
        paddingInsets.bottom + borderWidth,
        paddingInsets.right + borderWidth
    );
  }

  @Override
  public boolean isBorderOpaque() {
    return false;
  }

  @Override
  public void paintBorder(Component c, Graphics origGraphics, int x, int y, int width, int height) {
    final var g = (Graphics2D) origGraphics.create();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setStroke(new BasicStroke(1));

    for (int i = 0; i < borderWidth; ++i) {
      double alpha = ((i + 1.0f) / borderWidth) * MAX_SHADOW_ALPHA;
      g.setColor(new Color(0, 0, 0, (float) Math.pow(alpha, 2.5)));

      g.drawRoundRect(i, i, width - (i * 2), height - (i * 2), i * 2, i * 2);
    }

    g.dispose();
  }
}
