package fi.aalto.cs.apluscourses.ui.temp

import com.intellij.ui.JBColor
import com.intellij.ui.components.panels.OpaquePanel
import com.intellij.util.ui.JBUI
import fi.aalto.cs.apluscourses.MyBundle.message
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Cursor
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel

class ReplBannerPanel : JPanel(BorderLayout()) {
    private var isPermanentlyHidden = false

    private val containerPanel: JPanel

    /**
     * Constructor for the banner.
     */
    init {
        val infoText = JLabel(message("ui.repl.warning.description"))
        val dontShowOnceText = JLabel(message("ui.repl.warning.ignoreOnce"))
        val neverAskAgainText = JLabel(message("ui.repl.warning.ignoreAlways"))

        dontShowOnceText.setForeground(JBColor.BLUE)
        dontShowOnceText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
        dontShowOnceText.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                isPermanentlyHidden = true // TODO
                isVisible = false
            }
        })

        neverAskAgainText.setForeground(JBColor.BLUE)
        neverAskAgainText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
        neverAskAgainText.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
//        PluginSettings.getInstance().setHideReplModuleChangedWarning(true);
                isVisible = false
            }
        })

        containerPanel = OpaquePanel(FlowLayout(FlowLayout.LEFT))
        containerPanel.setBorder(JBUI.Borders.empty(5, 0, 5, 5))
        containerPanel.minimumSize = Dimension(0, 0)
        containerPanel.add(infoText)
        containerPanel.add(JLabel("|"))
        containerPanel.add(dontShowOnceText)
        containerPanel.add(JLabel("|"))
        containerPanel.add(neverAskAgainText)

        add(containerPanel)

        setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border()),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)
            )
        )
    }

    override fun setVisible(isVisible: Boolean) {
        if (isVisible) {
            val bgColor = JBColor(Color(200, 0, 0), Color(100, 0, 0))
            containerPanel.setBackground(bgColor)
            setBackground(bgColor)
        }

        //    super.setVisible(isVisible
//        && !isPermanentlyHidden
//        && !PluginSettings.getInstance().shouldHideReplModuleChangedWarning());
    }
}
