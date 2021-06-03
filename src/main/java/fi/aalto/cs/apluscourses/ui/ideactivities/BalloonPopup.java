package fi.aalto.cs.apluscourses.ui.ideactivities;

import com.intellij.util.ui.JBUI;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BalloonPopup extends JPanel {
  private final @NotNull Component anchorComponent;

  /**
   * Creates a popup with the given text. The popup is permanently attached to the specified
   * component. Optionally, an icon can be provided which will be displayed to the left of
   * the popup's title.
   */
  public BalloonPopup(@NotNull Component anchorComponent, @NotNull String title,
                      @NotNull String message, @Nullable Icon icon) {
    this.anchorComponent = anchorComponent;

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBorder(new EmptyBorder(JBUI.insets(0, 5, 10, 5)));

    // introduce a limit to the popup's width (so it doesn't take the entire screen width)
    setMaximumSize(new Dimension(500, 0));

    var titleText = new JLabel("<html><h1>" + title + "</h1></html>");
    titleText.setIcon(icon);

    var titlePanel = new JPanel();
    titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
    titlePanel.setAlignmentX(LEFT_ALIGNMENT);
    titlePanel.add(titleText);
    add(titlePanel);

    var messageText = new JLabel("<html>" + message + "</html>");
    messageText.setAlignmentX(LEFT_ALIGNMENT);
    add(messageText);

    recalculateBounds();
  }

  @Override
  public boolean isVisible() {
    return anchorComponent.isShowing();
  }

  /**
   * Recomputes the popups bounds and triggers a reposition if needed. Should ideally be called
   * every time anything changes in the parent frame.
   */
  public void recalculateBounds() {
    // the origin of the component that this popup is attached to must be converted to the
    // overlay pane's coordinate system, because that overlay uses a null layout and requires
    // that this popup specify its bounds
    var componentWindowPos = SwingUtilities.convertPoint(anchorComponent, 0, 0, getParent());

    var maxSize = getMaximumSize();
    var prefSize = getPreferredSize();

    var windowSize = JOptionPane.getRootFrame().getSize();
    var componentSize = anchorComponent.getSize();

    int popupWidth = Integer.min(maxSize.width, prefSize.width);
    int popupHeight = prefSize.height;

    int availableSizeLeft = componentWindowPos.x;
    int availableSizeRight = windowSize.width - (componentWindowPos.x + componentSize.width);
    int availableSizeTop = componentWindowPos.y;
    int availableSizeBottom = windowSize.height - (componentWindowPos.y + componentSize.height);

    int mostHorizontalSpace = Integer.max(availableSizeLeft, availableSizeRight);
    int mostVerticalSpace = Integer.max(availableSizeTop, availableSizeBottom);

    boolean positionHorizontally = mostHorizontalSpace > mostVerticalSpace;

    int popupX;
    int popupY;

    if (positionHorizontally) {
      if (availableSizeRight > availableSizeLeft) {
        popupX = componentWindowPos.x + anchorComponent.getWidth() + 5;
      } else {
        popupX = componentWindowPos.x - popupWidth - 5;
      }

      popupY = componentWindowPos.y + (anchorComponent.getHeight() - popupHeight) / 2;
    } else {
      if (availableSizeBottom > availableSizeTop) {
        popupY = componentWindowPos.y + anchorComponent.getHeight() + 5;
      } else {
        popupY = componentWindowPos.y - popupHeight - 5;
      }

      popupX = componentWindowPos.x + (anchorComponent.getWidth() - popupWidth) / 2;
    }

    setBounds(popupX, popupY, popupWidth, popupHeight);
  }
}
