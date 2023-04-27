package fi.aalto.cs.apluscourses.ui.tutorials;

import icons.PluginIcons;
import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.jetbrains.annotations.NotNull;

public class NormalBalloon extends JPanel implements Balloon {
  public static final int POPUP_MARGIN = 20;
  public static final int PROXIMITY_DISTANCE = 40;

  private static final Color COLOR = new Color(PluginIcons.ACCENT_COLOR).darker().darker();
  private static final int MAX_WIDTH = 300;

  private final boolean keepVisible;
  private final @NotNull SupportedPiece anchor;
  private final @NotNull JLabel titleLabel;
  private final @NotNull JLabel messageLabel;
  private final @NotNull AnimatedValue mOpacity;

  private final @NotNull NormalBalloon.MouseEventListener mouseEventListener = new MouseEventListener();
  private boolean isReleased = false;

  public NormalBalloon(boolean keepVisible,
                       @NotNull SupportedPiece anchor,
                       @NotNull String title,
                       @NotNull String message,
                       @NotNull Action @NotNull [] actions) {
    this.keepVisible = keepVisible;
    this.anchor = anchor;
    mOpacity = new AnimatedValueImpl(50, this::repaint);

    setOpaque(false);
    //setUI(null);
    setBorder(new EmptyBorder(10, 10, 10, 10));

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    // introduce a limit to the popup's width (so it doesn't take the entire screen width)
    titleLabel = new JLabel("<html><h3>" + title + "</h3></html>");
    //titleLabel.setIcon(icon);

    var titleBox = Box.createHorizontalBox();
    titleBox.setOpaque(false);
    titleBox.setAlignmentX(LEFT_ALIGNMENT);
    titleBox.add(titleLabel);
    add(titleBox);

    messageLabel = new JLabel("<html>" + message + "</html>");
    messageLabel.setAlignmentX(LEFT_ALIGNMENT);
    add(messageLabel);

    if (actions.length > 0) {
      var buttonBox = Box.createHorizontalBox();
      buttonBox.setAlignmentX(LEFT_ALIGNMENT);
      buttonBox.add(Box.createHorizontalGlue());
      for (var action : actions) {
        var button = new JButton(action);
        button.setAlignmentX(RIGHT_ALIGNMENT);
        buttonBox.add(button);
      }
      add(buttonBox);
    }
  }

  public void init() {
    if (isReleased) {
      throw new IllegalStateException();
    }
    Toolkit.getDefaultToolkit().addAWTEventListener(mouseEventListener, AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
  }

  public void release() {
    isReleased = true;
    Toolkit.getDefaultToolkit().removeAWTEventListener(mouseEventListener);
    anchor.removeSupporter(this);
  }

  public void recalculateBounds() {
    var anchorBounds = anchor.getArea(getParent()).getBounds();

    final var windowSize = JOptionPane.getRootFrame().getSize();

    final int popupWidth = Math.min(getPreferredSize().width, MAX_WIDTH);
    final int popupHeight = getMinimumSize().height;

    final int availableSizeLeft = anchorBounds.x;
    final int availableSizeRight = windowSize.width - (anchorBounds.x + anchorBounds.width);
    final int availableSizeTop = anchorBounds.y;
    final int availableSizeBottom = windowSize.height - (anchorBounds.y + anchorBounds.height);

    final int mostHorizontalSpace = Integer.max(availableSizeLeft, availableSizeRight);
    final int mostVerticalSpace = Integer.max(availableSizeTop, availableSizeBottom);

    final boolean canPlaceHorizontally = mostHorizontalSpace > popupWidth + 2 * POPUP_MARGIN;
    final boolean canPlaceVertically = mostVerticalSpace > popupHeight + 2 * POPUP_MARGIN;

    int popupX;
    int popupY;

    if (!canPlaceHorizontally && !canPlaceVertically) {
      // if there's no space on any side of the component, place the popup on the component
      popupX = anchorBounds.x + anchorBounds.width - popupWidth - POPUP_MARGIN;
      popupY = anchorBounds.y + POPUP_MARGIN;
    } else {
      if (!canPlaceVertically || (mostHorizontalSpace > mostVerticalSpace && canPlaceHorizontally)) {
        if (availableSizeRight > availableSizeLeft) {
          popupX = anchorBounds.x + anchorBounds.width + 5;
        } else {
          popupX = anchorBounds.x - popupWidth - 5;
        }

        popupY = anchorBounds.y + (anchorBounds.height - popupHeight) / 2;
      } else {
        if (availableSizeBottom > availableSizeTop) {
          popupY = anchorBounds.y + anchorBounds.height + 5;
        } else {
          popupY = anchorBounds.y - popupHeight - 5;
        }

        popupX = anchorBounds.x + (anchorBounds.width - popupWidth) / 2;
      }
    }

    setBounds(popupX, popupY, popupWidth, popupHeight);
  }

  @Override
  public void paint(Graphics g) {
    if (isReleased) {
      return;
    }
    if (anchor.isActive()) {
      opacity().fadeIn();
    } else if (keepVisible) {
      opacity().dim();
    } else {
      opacity().fadeOut();
    }

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setComposite(AlphaComposite.SrcOver.derive(opacity().get()));
    super.paint(g2);
    g2.dispose();
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

    g2.setColor(COLOR);

    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

    g2.dispose();
  }

  @Override
  public void revalidate() {
    if (isReleased || getParent() == null) {
      return;
    }
    recalculateBounds();
    super.revalidate();
  }

  @Override
  public @NotNull AnimatedValue opacity() {
    return mOpacity;
  }

  private boolean isCloseTo(@NotNull Point point) {
    var bounds = new Rectangle(getSize());
    bounds.grow(PROXIMITY_DISTANCE, PROXIMITY_DISTANCE);
    return bounds.contains(point);
  }

  private class MouseEventListener implements AWTEventListener {
    @Override
    public void eventDispatched(AWTEvent event) {
      var mouseEvent = (MouseEvent) event;
      var point = mouseEvent.getLocationOnScreen();
      SwingUtilities.convertPointFromScreen(point, NormalBalloon.this);
      if (isCloseTo(point) && opacity().get() > 0f) {
        anchor.addSupporter(NormalBalloon.this);
      } else {
        anchor.removeSupporter(NormalBalloon.this);
      }
    }
  }
}
