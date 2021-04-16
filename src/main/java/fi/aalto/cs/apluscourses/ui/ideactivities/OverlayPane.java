package fi.aalto.cs.apluscourses.ui.ideactivities;

import com.intellij.ui.JBColor;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

public class OverlayPane extends JPanel {
  private static OverlayPane activeOverlay = null;

  private final JRootPane associatedRootPane;
  private final List<Component> exemptComponents;

  private void revalidateFrame() {
    getRootPane().revalidate();
    getRootPane().repaint();
  }

  @Override
  protected void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);

    // use Graphics's copy so as not to influence subsequent calls using the same Graphics object
    Graphics2D g = (Graphics2D) graphics.create();
    g.setComposite(AlphaComposite.SrcOver.derive(0.7f));
    g.setColor(JBColor.BLACK);

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
    return associatedRootPane.getContentPane().getWidth();
  }

  @Override
  public int getHeight() {
    return associatedRootPane.getContentPane().getHeight();
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

  public static OverlayPane installOverlay() {
    if (activeOverlay != null) {
      throw new IllegalStateException("An overlay is already installed");
    }

    activeOverlay = new OverlayPane();
    activeOverlay.getRootPane().getContentPane().add(activeOverlay);
    activeOverlay.revalidateFrame();

    return activeOverlay;
  }

  public static void removeOverlay() {
    if (activeOverlay == null) {
      throw new IllegalStateException("No overlay is currently installed");
    }

    activeOverlay.getRootPane().getContentPane().remove(activeOverlay);
    activeOverlay.revalidateFrame();
  }

  private OverlayPane() {
    var rootFrame = (JFrame) JOptionPane.getRootFrame();

    associatedRootPane = rootFrame.getRootPane();
    exemptComponents = new ArrayList<>();

    setLayout(null);
  }
}
