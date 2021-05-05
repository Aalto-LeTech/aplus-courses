package fi.aalto.cs.apluscourses.ui.ideactivities;

import com.intellij.util.concurrency.annotations.RequiresEdt;
import icons.PluginIcons;
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
import org.jetbrains.annotations.NotNull;

public class OverlayPane extends JPanel {
  private static OverlayPane activeOverlay = null;

  // A high value (> 500) allows us to place the overlay pretty much above every other component
  private static final int PANE_Z_ORDER = 20000;

  private final JRootPane associatedRootPane;
  private final Set<Component> exemptComponents;
  private final Set<BalloonPopup> popups;

  private void revalidatePane() {
    getRootPane().revalidate();
    getRootPane().repaint();
  }

  @Override
  protected void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);

    for (var c : popups) {
      c.recalculateBounds();
    }

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

    for (var c : popups) {
      // popups are already places in the overlay's coordinate system
      var componentRect = new Rectangle(c.getX(), c.getY(), c.getWidth(), c.getHeight());
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

  /**
   * Installs the overlay.
   */
  @RequiresEdt
  public static void installOverlay() {
    if (isOverlayInstalled()) {
      throw new IllegalStateException("An overlay is already installed");
    }

    activeOverlay = new OverlayPane();
    activeOverlay.getRootPane().getLayeredPane().add(activeOverlay, PANE_Z_ORDER);
    activeOverlay.revalidatePane();
  }

  /**
   * Removes the overlay.
   */
  @RequiresEdt
  public static void removeOverlay() {
    if (!isOverlayInstalled()) {
      throw new IllegalStateException("No overlay is currently installed");
    }

    activeOverlay.getRootPane().getLayeredPane().remove(activeOverlay);
    for (var c : activeOverlay.popups) {
      activeOverlay.getRootPane().getLayeredPane().remove(c);
    }
    activeOverlay.revalidatePane();

    activeOverlay = null;
  }

  /**
   * Marks a component not to be dimmed.
   */
  public static void showComponent(Component c) {
    if (!isOverlayInstalled()) {
      throw new IllegalStateException("No overlay is currently installed");
    }

    activeOverlay.exemptComponents.add(c);
    activeOverlay.revalidatePane();
  }

  /**
   * Adds a popup to a specified component.
   */
  @RequiresEdt
  public static @NotNull BalloonPopup addPopup(@NotNull Component c, @NotNull String title,
                                               @NotNull String message) {
    if (!isOverlayInstalled()) {
      throw new IllegalStateException("No overlay is currently installed");
    }

    var popup = new BalloonPopup(c, title, message, PluginIcons.A_PLUS_OPTIONAL_PRACTICE);
    activeOverlay.popups.add(popup);
    activeOverlay.getRootPane().getLayeredPane().add(popup, PANE_Z_ORDER + 1);
    activeOverlay.revalidatePane();

    return popup;
  }

  /**
   * Resets the overlay to its original state, i.e. removes all popups and dims all components.
   */
  @RequiresEdt
  public static void resetOverlay() {
    if (!isOverlayInstalled()) {
      return;
    }

    activeOverlay.exemptComponents.clear();
    for (var c : activeOverlay.popups) {
      activeOverlay.getRootPane().getLayeredPane().remove(c);
    }
    activeOverlay.popups.clear();
    activeOverlay.revalidatePane();
  }

  private OverlayPane() {
    var rootFrame = (JFrame) JOptionPane.getRootFrame();

    associatedRootPane = rootFrame.getRootPane();
    exemptComponents = new HashSet<>();
    popups = new HashSet<>();

    setLayout(null);
  }
}
