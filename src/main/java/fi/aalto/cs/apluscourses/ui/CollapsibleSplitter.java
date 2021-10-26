package fi.aalto.cs.apluscourses.ui;

import com.intellij.ui.JBSplitter;
import com.intellij.util.ui.UIUtil;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public class CollapsibleSplitter {
  private final List<Splitter> splitters = new ArrayList<>();
  private static final Icon expandIcon = UIUtil.getTreeExpandedIcon();
  private static final Icon collapseIcon = UIUtil.getTreeCollapsedIcon();

  /**
   * Constructor.
   */
  public CollapsibleSplitter(@NotNull ToolbarPanel... toolbarPanels) {
    for (var toolbarPanel : toolbarPanels) {
      this.add(toolbarPanel);
    }
  }

  public void add(@NotNull ToolbarPanel toolbarPanel) {
    splitters.add(new Splitter(toolbarPanel.getBasePanel(), toolbarPanel.getToolbar()));
    splitters.forEach(Splitter::resetSize);
  }

  public JBSplitter getFirstSplitter() {
    return splitters.get(0).getJbSplitter();
  }

  public JBSplitter getLastSplitter() {
    return splitters.get(splitters.size() - 1).getJbSplitter();
  }

  private class Splitter {
    private boolean collapsed = true;
    private final JButton collapseButton;
    private final JBSplitter jbSplitter;

    protected Splitter(@NotNull JPanel panel, @NotNull JPanel toolbar) {
      panel.setMinimumSize(toolbar.getPreferredSize());

      this.jbSplitter = new JBSplitter(true);
      jbSplitter.setFirstComponent(panel);

      final Dimension buttonDimension = getButtonDimension();
      this.collapseButton = new JButton();
      collapseButton.setOpaque(false);
      collapseButton.setBorderPainted(false);
      collapseButton.setBackground(toolbar.getBackground());
      collapseButton.setSize(buttonDimension);
      collapseButton.setPreferredSize(buttonDimension);
      collapseButton.setMinimumSize(buttonDimension);
      collapseButton.setMaximumSize(buttonDimension);
      collapseButton.setFocusable(true);

      collapseButton.addActionListener(e -> toggleCollapsed());

      toolbar.add(collapseButton, 0);

      if (!splitters.isEmpty()) {
        getLastSplitter().setSecondComponent(jbSplitter);
      }
      jbSplitter.setSecondComponent(new JPanel());

      toggleCollapsed();
    }

    private Dimension getButtonDimension() {
      return new Dimension(expandIcon.getIconWidth(), expandIcon.getIconHeight());
    }

    protected void resetSize() {
      if (splitters.isEmpty() || jbSplitter == getLastSplitter()) {
        jbSplitter.setProportion(1);
      } else {
        jbSplitter.setProportion(1.0f / (splitters.size() - splitters.indexOf(this)));
      }
    }

    private void toggleCollapsed() {
      if (collapsed) {
        collapseButton.setIcon(expandIcon);
        resetSize();
      } else {
        collapseButton.setIcon(collapseIcon);
        jbSplitter.setProportion(0);
      }
      jbSplitter.setResizeEnabled(collapsed);
      collapsed = !collapsed;
    }

    protected JBSplitter getJbSplitter() {
      return jbSplitter;
    }
  }
}
