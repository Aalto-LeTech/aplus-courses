package fi.aalto.cs.apluscourses.ui

import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.JBUI.CurrentTheme.NotificationError
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JPanel

class BannerPanel(text: String, bannerType: BannerType) : JPanel(BorderLayout()) {
    private val errorForeground: Color = NotificationError.foregroundColor()
    private val errorBackground: Color = NotificationError.backgroundColor()
    private val errorBorderColor: Color = NotificationError.borderColor()

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
