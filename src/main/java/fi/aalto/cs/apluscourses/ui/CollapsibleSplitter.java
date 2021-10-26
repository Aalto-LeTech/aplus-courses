package fi.aalto.cs.apluscourses.ui;

import com.intellij.ui.JBSplitter;
import com.intellij.util.ui.UIUtil;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    splitters.add(new Splitter(toolbarPanel));
    splitters.forEach(Splitter::expand);
  }

  /**
   * Collapses all splitters by the titles given in the string, separated by ";".
   */
  public void collapseByTitles(@Nullable String titles) {
    if (titles == null) {
      return;
    }
    Arrays.stream(titles.split(";"))
        .forEach(title -> splitters.stream().filter(splitter -> title.equals(splitter.getTitle()))
            .forEach(Splitter::collapse));
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
    private final String title;

    protected Splitter(@NotNull ToolbarPanel toolbarPanel) {
      var panel = toolbarPanel.getBasePanel();
      var toolbar = toolbarPanel.getToolbar();
      panel.setMinimumSize(toolbar.getPreferredSize());

      this.title = toolbarPanel.getTitle();

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
      toolbar.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

      if (!splitters.isEmpty()) {
        getLastSplitter().setSecondComponent(jbSplitter);
      }
      jbSplitter.setSecondComponent(new JPanel());

      toggleCollapsed();
    }

    private Dimension getButtonDimension() {
      return new Dimension(expandIcon.getIconWidth(), expandIcon.getIconHeight());
    }

    protected void expand() {
      if (splitters.isEmpty() || jbSplitter == getLastSplitter()) {
        jbSplitter.setProportion(1);
      } else {
        jbSplitter.setProportion(1.0f / (splitters.size() - splitters.indexOf(this)));
      }
      collapseButton.setIcon(expandIcon);
      jbSplitter.setResizeEnabled(true);
      collapsed = false;
    }

    protected void collapse() {
      jbSplitter.setProportion(0);
      collapseButton.setIcon(collapseIcon);
      jbSplitter.setResizeEnabled(false);
      collapsed = true;
    }

    private void toggleCollapsed() {
      if (collapsed) {
        expand();
        PluginSettings.getInstance().setExpanded(title);
      } else {
        collapse();
        PluginSettings.getInstance().setCollapsed(title);
      }
    }

    protected JBSplitter getJbSplitter() {
      return jbSplitter;
    }

    public String getTitle() {
      return title;
    }
  }
}
