package fi.aalto.cs.apluscourses.ui;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.labels.LinkLabel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.jetbrains.annotations.NotNull;

public class AboutDialog {

  private static final String A_COURSES_PLUGIN_PAGE = "https://plugins.jetbrains.com/plugin/13634-a-courses";

  private AboutDialog() {

  }

  /**
   * Displays a small pop up window containing basic information about the plugin.
   */
  public static void display() {
    JOptionPane.showMessageDialog(null,
        createAboutInnerContainer(),
        getText("ui.aboutDialog.title"),
        JOptionPane.INFORMATION_MESSAGE);
  }

  @NotNull
  private static JPanel createAboutInnerContainer() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    JBLabel label = createAboutTextLabel();
    LinkLabel<Object> linkLabel = createPluginWebsiteLinkLabel();
    panel.add(label);
    panel.add(Box.createVerticalStrut(linkLabel.getFont().getSize()));
    panel.add(linkLabel);

    return panel;
  }

  @NotNull
  private static LinkLabel<Object> createPluginWebsiteLinkLabel() {
    LinkLabel<Object> linkLabel = new LinkLabel<>(
        getText("ui.aboutDialog.website"),
        AllIcons.Ide.External_link_arrow,
        (first, second) ->
            BrowserUtil.browse(A_COURSES_PLUGIN_PAGE));
    linkLabel.setIconTextGap(0);
    linkLabel.setHorizontalTextPosition(SwingConstants.LEFT);

    return linkLabel;
  }

  @NotNull
  private static JBLabel createAboutTextLabel() {
    JBLabel label = new JBLabel();
    label.setText(getText("ui.aboutDialog.description"));

    return label;
  }
}
