package fi.aalto.cs.apluscourses.ui.tutorials;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OverlayPane extends JComponent implements AWTEventListener {
  // A high value (> 500) allows us to place the overlay pretty much above every other component
  private static final int PANE_Z_ORDER = 20000;

  private JLayeredPane layeredPane;
  private final Map<@NotNull Object, @NotNull Highlighting> highlightings = new LinkedHashMap<>();

  private final Set<@NotNull Component> balloons = new HashSet<>();

  private void revalidatePane() {
    if (layeredPane == null) {
      return;
    }
    layeredPane.revalidate();
    layeredPane.repaint();
    balloons.forEach(Component::revalidate);
  }

  @Override
  public void paintComponent(Graphics graphics) {
    for (var highlighting : highlightings.values()) {
      if (highlighting.paint(graphics)) {
        return;
      }
    }
  }

  @Override
  public int getWidth() {
    return layeredPane.getWidth();
  }

  @Override
  public int getHeight() {
    return layeredPane.getHeight();
  }

  @Override
  public Dimension getSize() {
    return new Dimension(getWidth(), getHeight());
  }

  @Override
  public Dimension getPreferredSize() {
    return getSize();
  }

  @Override
  public Dimension getMinimumSize() {
    return getSize();
  }

  @Override
  public Dimension getMaximumSize() {
    return getSize();
  }

  @Override
  public Rectangle getBounds() {
    return new Rectangle(0, 0, getWidth(), getHeight());
  }

  @Override
  public boolean isOpaque() {
    return false;
  }

  @Override
  public boolean isVisible() {
    return true;
  }

  public void init() {
    layeredPane = ((JFrame) JOptionPane.getRootFrame()).getLayeredPane();
    layeredPane.setLayer(this, PANE_Z_ORDER);
    layeredPane.add(this);
    revalidatePane();
    Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK);
  }

  public void release() {
    Toolkit.getDefaultToolkit().removeAWTEventListener(this);
    layeredPane.remove(this);
    revalidatePane();
    layeredPane = null;
  }

  public void addHighlight(@NotNull Object key, @NotNull Piece piece) {
    var highlighting = highlightings.get(key);
    if (highlighting != null) {
      highlighting.addPiece(piece);
      revalidatePane();
    }
  }

  public void removeHighlight(@NotNull Object key, @NotNull Piece piece) {
    var highlighting = highlightings.get(key);
    if (highlighting != null) {
      highlighting.removePiece(piece);
      revalidatePane();
    }
  }

  public void addBalloon(@NotNull Component component) {
    balloons.add(component);
    layeredPane.setLayer(component, PANE_Z_ORDER + 100);
    layeredPane.add(component);
    revalidatePane();
  }

  public void removeBalloon(@NotNull Component balloon) {
    balloons.remove(balloon);
    layeredPane.remove(balloon);
    revalidatePane();
  }

  public void defineHighlighting(@NotNull Object key,
                                 @Nullable Color color,
                                 float alpha,
                                 @Nullable Color color2,
                                 float alpha2,
                                 boolean secondForBackground) {
    highlightings.put(key, new Highlighting(this,
        color == null ? new NullAreaPainter() : new GradientAreaPainter(color, alpha, this::revalidatePane),
        color2 == null ? new NullAreaPainter() : new BasicAreaPainter(color2, alpha2, this::revalidatePane),
        secondForBackground));
  }

  @Override
  public void eventDispatched(AWTEvent event) {
    revalidatePane();
  }
}
