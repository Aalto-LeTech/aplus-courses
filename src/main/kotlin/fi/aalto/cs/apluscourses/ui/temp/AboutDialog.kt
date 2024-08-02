package fi.aalto.cs.apluscourses.ui.temp

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.components.labels.LinkListener
import com.intellij.util.ui.JBUI
import icons.PluginIcons
import java.awt.Component
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.border.Border

class AboutDialog(project: Project) : DialogWrapper(project) {
    /**
     * Constructor.
     */
    init {
        setResizable(false)
        setSize(500, 500)
        //    setTitle(getText("ui.aboutDialog.title"));
        init()
    }

    override fun createCenterPanel(): JComponent? {
        return createAboutInnerContainer()
    }

    override fun createNorthPanel(): JComponent? {
        return createLogoImageLabel()
    }

    override fun createSouthPanel(): JComponent {
        return createFooterLabel()
    }

    override fun createSouthAdditionalPanel(): JPanel? {
        return super.createSouthAdditionalPanel()
    }

    override fun createContentPaneBorder(): Border? {
        return null
    }

    private fun createAboutInnerContainer(): JPanel {
        val box = Box.createVerticalBox()

        val version = createVersionTextLabel()
        val about = createAboutTextLabel()
        val authors = createAuthorsTextLabel()
        val attributes: JBLabel = AboutDialog.Companion.createAttributesTextLabel()
        //    LinkLabel<Object> linkLabelPlugin = createLinkLabel(getText("ui.aboutDialog.website"), A_COURSES_PLUGIN_PAGE);
//    LinkLabel<Object> linkLabelGithub = createLinkLabel(getText("ui.aboutDialog.GithubWebsite"), GITHUB_PAGE);
//    LinkLabel<Object> linkLabelAPlus = createLinkLabel(getText("ui.aboutDialog.APlusWebsite"), A_PLUS_PAGE);
//    var items = List.of(version, about, authors, attributes, linkLabelPlugin, linkLabelGithub, linkLabelAPlus);
//    items.forEach(item -> item.setAlignmentX(Component.LEFT_ALIGNMENT));
        box.add(createFiller())
        box.add(version)
        box.add(createFiller())
        box.add(about)
        box.add(createFiller())
        //    box.add(linkLabelPlugin);
//    box.add(linkLabelGithub);
//    box.add(linkLabelAPlus);
        box.add(createFiller())
        box.add(authors)
        box.add(createFiller())
        box.add(attributes)

        val centerPanel = JPanel()
        centerPanel.setLayout(BoxLayout(centerPanel, BoxLayout.LINE_AXIS))
        val button = createJButtonForAction(DialogWrapperExitAction("Close", CLOSE_EXIT_CODE))
        centerPanel.add(Box.createHorizontalGlue())
        centerPanel.add(button)
        centerPanel.add(Box.createHorizontalGlue())
        box.add(centerPanel)
        box.add(createFiller())

        return JBUI.Panels.simplePanel(box)
    }

    private fun createFiller(): Component {
        return Box.createVerticalStrut(15)
    }

    private fun createLogoImageLabel(): JBLabel {
        val icon = PluginIcons.A_PLUS_COURSES_BANNER
        return JBLabel(icon, SwingConstants.CENTER)
    }

    private fun createFooterLabel(): JBLabel {
        val icon = PluginIcons.A_PLUS_COURSES_FOOTER
        return JBLabel(icon, SwingConstants.CENTER)
    }

    private fun createLinkLabel(text: String, link: String): LinkLabel<Any?> {
        val linkLabel = LinkLabel<Any?>(text, AllIcons.Ide.External_link_arrow,
            LinkListener { first: LinkLabel<Any?>?, second: Any? -> BrowserUtil.browse(link) })
        linkLabel.setIconTextGap(0)
        linkLabel.setHorizontalTextPosition(SwingConstants.LEFT)

        return linkLabel
    }

    private fun createVersionTextLabel(): JBLabel {
        val label = JBLabel()
        //    String version = BuildInfo.INSTANCE.pluginVersion.toString();
//    label.setText(getAndReplaceText("ui.aboutDialog.version", version));
        return label
    }

    private fun createAboutTextLabel(): JBTextArea {
        return createTextArea("getText(ui.aboutDialog.description)")
    }

    private fun createAuthorsTextLabel(): JBTextArea {
        return createTextArea("getText(ui.aboutDialog.authors)")
    }

    private fun createTextArea(text: String): JBTextArea {
        val textArea = JBTextArea()
        textArea.setText(text)
        textArea.setWrapStyleWord(true)
        textArea.setLineWrap(true)
        textArea.setEditable(false)
        textArea.setMaximumSize(Dimension(420, textArea.getMaximumSize().height))
        textArea.setBackground(JBColor.background())
        textArea.setFont(JBUI.Fonts.label())
        return textArea
    }

    companion object {
        private const val A_COURSES_PLUGIN_PAGE = "https://plugins.jetbrains.com/plugin/13634-a-courses"
        private const val A_PLUS_PAGE = "https://plus.cs.aalto.fi/"
        private const val GITHUB_PAGE = "https://github.com/Aalto-LeTech/aplus-courses"

        private fun createAttributesTextLabel(): JBLabel {
            val label = JBLabel()
            //    label.setText(getText("ui.aboutDialog.attributes"));
            label.setCopyable(true)
            return label
        }
    }
}
