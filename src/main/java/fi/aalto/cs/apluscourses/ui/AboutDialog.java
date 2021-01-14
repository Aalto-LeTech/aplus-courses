package fi.aalto.cs.apluscourses.ui;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.labels.LinkLabel;

import fi.aalto.cs.apluscourses.utils.BuildInfo;
import icons.PluginIcons;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jetbrains.annotations.NotNull;

public class AboutDialog {

  private static final String A_COURSES_PLUGIN_PAGE = "https://plugins.jetbrains.com/plugin/13634-a-courses";
  private static final String A_PLUS_PAGE = "https://plus.cs.aalto.fi/";
  private static final String GITHUB_PAGE = "https://github.com/Aalto-LeTech/intellij-plugin";

  private AboutDialog() {

  }

  /**
   * Displays a small pop up window containing basic information about the plugin.
   */
  public static void display() {

    JOptionPane.showMessageDialog(null,
        createAboutInnerContainer(),
        getText("ui.aboutDialog.title"),
        JOptionPane.PLAIN_MESSAGE);
  }

  @NotNull
  private static JPanel createAboutInnerContainer() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    JBLabel logo = createLogoImageLabel();
    JBLabel version = createVersionTextLabel();
    JBLabel about = createAboutTextLabel();
    JBLabel authors = createAuthorsTextLabel();
    JBLabel attributes = createAttributesTextLabel();
    LinkLabel<Object> linkLabel = createPluginWebsiteLinkLabel();
    LinkLabel<Object> linkLabelGithub = createGithubWebsiteLinkLabel();
    LinkLabel<Object> linkLabelAPlus = createAPlusWebsiteLinkLabel();
    panel.add(logo);
    panel.add(Box.createVerticalStrut(about.getFont().getSize()));
    panel.add(version);
    panel.add(Box.createVerticalStrut(about.getFont().getSize()));
    panel.add(about);
    panel.add(Box.createVerticalStrut(linkLabel.getFont().getSize()));
    panel.add(linkLabel);
    panel.add(linkLabelGithub);
    panel.add(linkLabelAPlus);
    panel.add(Box.createVerticalStrut(authors.getFont().getSize()));
    panel.add(authors);
    panel.add(Box.createVerticalStrut(authors.getFont().getSize()));
    panel.add(attributes);
    panel.setBorder(BorderFactory.createEmptyBorder(0,15,0,15));
    return panel;
  }

  private static JBLabel createLogoImageLabel() {
    final Icon icon = PluginIcons.A_PLUS_LOGO;
    return new JBLabel(icon, SwingConstants.CENTER);
  }

  private static JBLabel createAttributesTextLabel() {
    JBLabel label = new JBLabel();
    label.setText(getText("ui.aboutDialog.attributes"));
    label.setCopyable(true);
    return label;
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
  private static LinkLabel<Object> createAPlusWebsiteLinkLabel() {
    LinkLabel<Object> linkLabel = new LinkLabel<>(
        getText("ui.aboutDialog.APlusWebsite"),
        AllIcons.Ide.External_link_arrow,
        (first, second) ->
                BrowserUtil.browse(A_PLUS_PAGE));
    linkLabel.setIconTextGap(0);
    linkLabel.setHorizontalTextPosition(SwingConstants.LEFT);

    return linkLabel;
  }

  @NotNull
  private static LinkLabel<Object> createGithubWebsiteLinkLabel() {
    LinkLabel<Object> linkLabel = new LinkLabel<>(
        getText("ui.aboutDialog.GithubWebsite"),
        AllIcons.Ide.External_link_arrow,
        (first, second) ->
                BrowserUtil.browse(GITHUB_PAGE));
    linkLabel.setIconTextGap(0);
    linkLabel.setHorizontalTextPosition(SwingConstants.LEFT);

    return linkLabel;
  }

  @NotNull
  private static JBLabel createVersionTextLabel() {
    JBLabel label = new JBLabel();
    String version = BuildInfo.INSTANCE.version.toString();
    label.setText(getAndReplaceText("ui.aboutDialog.version", version));
    return label;
  }

  @NotNull
  private static JBLabel createAboutTextLabel() {
    JBLabel label = new JBLabel();
    label.setText(getText("ui.aboutDialog.description"));
    return label;
  }

  @NotNull
  private static JBLabel createAuthorsTextLabel() {
    JBLabel label = new JBLabel();
    label.setText(getText("ui.aboutDialog.authors"));
    return label;
  }
}
