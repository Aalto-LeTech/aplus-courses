package fi.aalto.cs.apluscourses.ui

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JPanel

class BannerPanel(text: String, bannerType: BannerType) : JPanel(BorderLayout()) {
    private val errorForeground: JBColor = JBColor.namedColor("Notification.ToolWindow.errorForeground")
    private val errorBackground: JBColor = JBColor.namedColor("Notification.ToolWindow.errorBackground")
    private val errorBorderColor: JBColor = JBColor.namedColor("Notification.ToolWindow.errorBorderColor")

    /**
     * Constructor for the banner.
     */
    init {
        val bannerText = JBLabel(text)
        bannerText.border = JBUI.Borders.empty(6)
        add(bannerText)
        when (bannerType) {
            BannerType.ERROR -> {
                bannerText.foreground = errorForeground
                background = errorBackground
                border = BorderFactory.createMatteBorder(0, 0, 1, 0, errorBorderColor)
            }
        }
    }

    override fun getWidth(): Int {
        return parent.width
    }

    enum class BannerType {
        ERROR,
//        WARNING,
//        INFO
    }

}
