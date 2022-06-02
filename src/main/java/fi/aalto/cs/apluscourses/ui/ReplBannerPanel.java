package fi.aalto.cs.apluscourses.ui;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.panels.OpaquePanel;
import com.intellij.util.ui.JBUI;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ReplBannerPanel extends JPanel {

  private boolean isPermanentlyHidden = false;

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
    dontShowOnceText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    dontShowOnceText.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent e) {
        isPermanentlyHidden = true;
        setVisible(false);
      }
      @Override
      public void mousePressed(MouseEvent e) {}
      @Override
      public void mouseReleased(MouseEvent e) {}
      @Override
      public void mouseEntered(MouseEvent e) {}
      @Override
      public void mouseExited(MouseEvent e) {}
    });

    neverAskAgainText.setForeground(JBColor.BLUE);
    neverAskAgainText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    neverAskAgainText.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent e) {
        PluginSettings.getInstance().setHideReplModuleChangedWarning(true);
        setVisible(false);
      }
      @Override
      public void mousePressed(MouseEvent e) {}
      @Override
      public void mouseReleased(MouseEvent e) {}
      @Override
      public void mouseEntered(MouseEvent e) {}
      @Override
      public void mouseExited(MouseEvent e) {}
    });

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

  @Override
  public void setVisible(boolean aFlag) {
    if (this.isPermanentlyHidden || PluginSettings.getInstance().shouldHideReplModuleChangedWarning()) {
      aFlag = false;
    }

    super.setVisible(aFlag);
  }
}
