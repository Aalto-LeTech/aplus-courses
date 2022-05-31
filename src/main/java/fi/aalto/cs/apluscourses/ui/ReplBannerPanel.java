package fi.aalto.cs.apluscourses.ui;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.panels.OpaquePanel;
import com.intellij.util.ui.JBUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public class ReplBannerPanel extends JPanel {

  /**
   * Constructor for the banner.
   */
  public ReplBannerPanel() {
    super(new BorderLayout());

    JPanel panel = new OpaquePanel(new FlowLayout(FlowLayout.LEFT));
    panel.setBorder(JBUI.Borders.empty(5, 0, 5, 5));
    panel.setMinimumSize(new Dimension(0, 0));

    JLabel infoText = new JLabel("You need to restart the REPL for the code changes to be applied.");
    JLabel dontShowOnceText = new JLabel("Ignore for this session");
    JLabel neverAskAgainText = new JLabel("Never ask again");

    dontShowOnceText.setForeground(JBColor.BLUE);
    neverAskAgainText.setForeground(JBColor.BLUE);

    panel.add(infoText);
    panel.add(new JLabel("|"));
    panel.add(dontShowOnceText);
    panel.add(new JLabel("|"));
    panel.add(neverAskAgainText);

    add(panel);

    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border()),
        BorderFactory.createEmptyBorder(0, 5, 0, 5))
    );
  }
}
