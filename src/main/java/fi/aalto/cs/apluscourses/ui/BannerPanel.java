package fi.aalto.cs.apluscourses.ui;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.panels.OpaquePanel;
import com.intellij.util.ui.JBUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public class BannerPanel extends JPanel {
  @NotNull
  private final JLabel bannerText = new JLabel();

  @NotNull
  private final JPanel containerPanel;

  /**
   * Constructor for the banner.
   */
  public BannerPanel() {
    super(new BorderLayout());

    containerPanel = new OpaquePanel(new BorderLayout());
    containerPanel.add(BorderLayout.CENTER, bannerText);
    containerPanel.setBorder(JBUI.Borders.empty(5, 0, 5, 5));
    containerPanel.setMinimumSize(new Dimension(0, 0));

    add(BorderLayout.CENTER, containerPanel);

    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border()),
        BorderFactory.createEmptyBorder(0, 10, 0, 10))
    );
  }

  public void setText(String text) {
    bannerText.setText(text);
  }

  public void setBannerColor(@NotNull Color color) {
    containerPanel.setBackground(color);
    setBackground(color);
  }
}
