package fi.aalto.cs.apluscourses.ui.ideactivities;

import com.intellij.util.ui.JBUI;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Component;

public class BalloonPopup extends JPanel {
  private final @NotNull Component anchorComponent;

  public BalloonPopup(@NotNull Component anchorComponent, @NotNull String title,
                      @NotNull String message, @Nullable Icon icon) {
    this.anchorComponent = anchorComponent;

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBorder(new EmptyBorder(JBUI.insets(0, 5, 10, 5)));

    var titleText = new JLabel("<html><h1>" + title + "</h1></html>");
    if (icon != null) {
      titleText.setIcon(icon);
    }

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

  private void recalculateBounds() {
    // the origin of the component that this popup is attached to must be converted to the
    // overlay pane's coordinate system, because that overlay uses a null layout and requires
    // that this popup specify its bounds

    var componentWindowPos = SwingUtilities.convertPoint(
        anchorComponent, anchorComponent.getX(), anchorComponent.getY(), getParent());

    var prefSize = getPreferredSize();

    int popupWidth = prefSize.width;
    int popupHeight = prefSize.height;

    int popupX = componentWindowPos.x + anchorComponent.getWidth() + 5;
    int popupY = componentWindowPos.y + (anchorComponent.getHeight() - popupHeight) / 2;

    setBounds(popupX, popupY, popupWidth, popupHeight);
  }
}
