package fi.aalto.cs.apluscourses.ui.ideactivities;

import com.intellij.ui.JBColor;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

public class OverlayPane extends JPanel {
  private static OverlayPane activeOverlay = null;

  private final JRootPane associatedRootPane;
  private final Set<Component> exemptComponents;

  private void revalidatePane() {
    getRootPane().revalidate();
    getRootPane().repaint();
  }

  @Override
  protected void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);

    // use Graphics's copy so as not to influence subsequent calls using the same Graphics object
    Graphics2D g = (Graphics2D) graphics.create();
    g.setComposite(AlphaComposite.SrcOver.derive(0.7f));
    g.setColor(Color.BLACK); // using JBColor.BLACK is wrong here

    var overlayArea = new Area(new Rectangle(0, 0, getWidth(), getHeight()));

    for (var c : exemptComponents) {
      // convertPoint is necessary because the component uses a different coordinate origin
      var windowPos = SwingUtilities.convertPoint(c, c.getX(), c.getY(), this);
      var componentRect = new Rectangle(windowPos.x, windowPos.y, c.getWidth(), c.getHeight());

      overlayArea.subtract(new Area(componentRect));
    }

    g.fill(overlayArea);
  }

  @Override
  public JRootPane getRootPane() {
    return associatedRootPane;
  }

  @Override
  public int getWidth() {
    return associatedRootPane.getLayeredPane().getWidth();
  }

  @Override
  public int getHeight() {
    return associatedRootPane.getLayeredPane().getHeight();
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
  public boolean isOpaque() {
    return false;
  }

  @Override
  public boolean isVisible() {
    return true;
  }

  public static boolean isOverlayInstalled() {
    return activeOverlay != null;
  }

  public static OverlayPane installOverlay() {
    if (isOverlayInstalled()) {
      throw new IllegalStateException("An overlay is already installed");
    }

    activeOverlay = new OverlayPane();
    activeOverlay.getRootPane().getLayeredPane().add(activeOverlay, 20000);
    activeOverlay.revalidatePane();

    return activeOverlay;
  }

  public static void removeOverlay() {
    if (!isOverlayInstalled()) {
      throw new IllegalStateException("No overlay is currently installed");
    }

    activeOverlay.getRootPane().getLayeredPane().remove(activeOverlay);
    activeOverlay.revalidatePane();

    activeOverlay = null;
  }

  public static void showComponent(Component c) {
    if (!isOverlayInstalled()) {
      throw new IllegalStateException("No overlay is currently installed");
    }

    activeOverlay.exemptComponents.add(c);
    activeOverlay.revalidatePane();
  }

  private OverlayPane() {
    var rootFrame = (JFrame) JOptionPane.getRootFrame();

    associatedRootPane = rootFrame.getRootPane();
    exemptComponents = new HashSet<>();

    setLayout(null);
  }
}
