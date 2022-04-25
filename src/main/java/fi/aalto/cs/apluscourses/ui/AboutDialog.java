package fi.aalto.cs.apluscourses.ui;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.util.ui.JBUI;
import fi.aalto.cs.apluscourses.utils.BuildInfo;
import icons.PluginIcons;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AboutDialog extends DialogWrapper {

  private static final String A_COURSES_PLUGIN_PAGE = "https://plugins.jetbrains.com/plugin/13634-a-courses";
  private static final String A_PLUS_PAGE = "https://plus.cs.aalto.fi/";
  private static final String GITHUB_PAGE = "https://github.com/Aalto-LeTech/aplus-courses";

  public AboutDialog(@NotNull Project project) {
    super(project);
    setResizable(false);
    setSize(500, 500);
    setTitle(getText("ui.aboutDialog.title"));
    init();
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    return createAboutInnerContainer();
  }

  @Override
  protected @Nullable JComponent createNorthPanel() {
    return createLogoImageLabel();
  }

  @Override
  protected JComponent createSouthPanel() {
    return createFooterLabel();
  }

  @Override
  protected @Nullable JPanel createSouthAdditionalPanel() {
    return super.createSouthAdditionalPanel();
  }

  @Override
  protected @Nullable Border createContentPaneBorder() {
    return null;
  }

  /**
   * Displays a small pop up window containing basic information about the plugin.
   */
  public static void display2() {

    JOptionPane.showMessageDialog(null,
        null,
        getText("ui.aboutDialog.title"),
        JOptionPane.PLAIN_MESSAGE);
  }

  @NotNull
  private JPanel createAboutInnerContainer() {
    var box = Box.createVerticalBox();

    JBLabel version = createVersionTextLabel();
    var about = createAboutTextLabel();
    var authors = createAuthorsTextLabel();
    var attributes = createAttributesTextLabel();
    LinkLabel<Object> linkLabel = createPluginWebsiteLinkLabel();
    LinkLabel<Object> linkLabelGithub = createGithubWebsiteLinkLabel();
    LinkLabel<Object> linkLabelAPlus = createAPlusWebsiteLinkLabel();
    var items = List.of(version, about, authors, attributes, linkLabel, linkLabelGithub, linkLabelAPlus);
    items.forEach(item -> item.setAlignmentX(Component.LEFT_ALIGNMENT));
    box.add(createFiller());
    box.add(version);
    box.add(createFiller());
    box.add(about);
    box.add(createFiller());
    box.add(linkLabel);
    box.add(linkLabelGithub);
    box.add(linkLabelAPlus);
    box.add(createFiller());
    box.add(authors);
    box.add(createFiller());
    box.add(attributes);

    var centerPanel = new JPanel();
    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.LINE_AXIS));
    var button = createJButtonForAction(new DialogWrapperExitAction("Close", CLOSE_EXIT_CODE));
    centerPanel.add(Box.createHorizontalGlue());
    centerPanel.add(button);
    centerPanel.add(Box.createHorizontalGlue());
    box.add(centerPanel);
    box.add(createFiller());

    return JBUI.Panels.simplePanel(box);
  }

  private Component createFiller() {
    return Box.createVerticalStrut(15);
  }

  private JBLabel createLogoImageLabel() {
    final Icon icon = PluginIcons.A_PLUS_COURSES_BANNER;
    return new JBLabel(icon, SwingConstants.CENTER);
  }

  private JBLabel createFooterLabel() {
    final Icon icon = PluginIcons.A_PLUS_COURSES_FOOTER;
    return new JBLabel(icon, SwingConstants.CENTER);
  }

  private static JBLabel createAttributesTextLabel() {
    JBLabel label = new JBLabel();
    label.setText(getText("ui.aboutDialog.attributes"));
    label.setCopyable(true);
    return label;
  }

  @NotNull
  private LinkLabel<Object> createPluginWebsiteLinkLabel() {
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
  private LinkLabel<Object> createAPlusWebsiteLinkLabel() {
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
  private LinkLabel<Object> createGithubWebsiteLinkLabel() {
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
  private JBLabel createVersionTextLabel() {
    JBLabel label = new JBLabel();
    String version = BuildInfo.INSTANCE.pluginVersion.toString();
    label.setText(getAndReplaceText("ui.aboutDialog.version", version));
    return label;
  }

  @NotNull
  private JBTextArea createAboutTextLabel() {
    return createTextArea(getText("ui.aboutDialog.description"));
  }

  @NotNull
  private JBTextArea createAuthorsTextLabel() {
    return createTextArea(getText("ui.aboutDialog.authors"));
  }

  @NotNull
  private JBTextArea createTextArea(@NotNull String text) {
    JBTextArea textArea = new JBTextArea();
    textArea.setText(text);
    textArea.setWrapStyleWord(true);
    textArea.setLineWrap(true);
    textArea.setEditable(false);
    textArea.setMaximumSize(new Dimension(420, textArea.getMaximumSize().height));
    textArea.setBackground(JBColor.background());
    return textArea;
  }
}
