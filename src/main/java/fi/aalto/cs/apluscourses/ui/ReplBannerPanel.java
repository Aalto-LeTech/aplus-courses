package fi.aalto.cs.apluscourses.ui;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.panels.OpaquePanel;
import com.intellij.util.ui.JBUI;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public class ReplBannerPanel extends JPanel {

  private boolean isPermanentlyHidden = false;

  @NotNull
  private final JPanel containerPanel;

  /**
   * Constructor for the banner.
   */
  public ReplBannerPanel() {
    super(new BorderLayout());

    final JLabel infoText = new JLabel(getText("ui.repl.warning.description"));
    final JLabel dontShowOnceText = new JLabel(getText("ui.repl.warning.ignoreOnce"));
    final JLabel neverAskAgainText = new JLabel(getText("ui.repl.warning.ignoreAlways"));

    dontShowOnceText.setForeground(JBColor.BLUE);
    dontShowOnceText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    dontShowOnceText.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        isPermanentlyHidden = true;
        setVisible(false);
      }
    });

    neverAskAgainText.setForeground(JBColor.BLUE);
    neverAskAgainText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    neverAskAgainText.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        PluginSettings.getInstance().setHideReplModuleChangedWarning(true);
        setVisible(false);
      }
    });

    containerPanel = new OpaquePanel(new FlowLayout(FlowLayout.LEFT));
    containerPanel.setBorder(JBUI.Borders.empty(5, 0, 5, 5));
    containerPanel.setMinimumSize(new Dimension(0, 0));
    containerPanel.add(infoText);
    containerPanel.add(new JLabel("|"));
    containerPanel.add(dontShowOnceText);
    containerPanel.add(new JLabel("|"));
    containerPanel.add(neverAskAgainText);

    add(containerPanel);

    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border()),
        BorderFactory.createEmptyBorder(0, 5, 0, 5))
    );
  }

  @Override
  public void setVisible(boolean isVisible) {
    if (isVisible) {
      final JBColor bgColor = new JBColor(new Color(200, 0, 0), new Color(100, 0, 0));
      containerPanel.setBackground(bgColor);
      setBackground(bgColor);
    }

    super.setVisible(isVisible
        && !isPermanentlyHidden
        && !PluginSettings.getInstance().shouldHideReplModuleChangedWarning());
  }
}
