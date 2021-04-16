package fi.aalto.cs.apluscourses.ui.ideactivities;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;

public class OverlayPane extends JPanel {
  private static OverlayPane activeOverlay = null;

  private final JRootPane associatedRootPane;
  private final List<Component> exemptComponents;

  private void revalidateFrame() {
    getRootPane().revalidate();
    getRootPane().repaint();
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
