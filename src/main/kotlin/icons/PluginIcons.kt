package icons

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader.getIcon
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.JBColor
import java.awt.Color
import javax.swing.Icon

/**
 * [Guide](https://www.jetbrains.org/intellij/sdk/docs/reference_guide/work_with_icons_and_images.html)<br></br>
 * [Icons source](https://jetbrains.design/intellij/resources/icons_list/)<br></br>
 * Accent colour: #FF0090(FF)<br></br>
 * (100% opacity) aka RGB(255, 0, 144) Light schema: #AFB1B3(FF) (100% opacity) Dark schema:
 * #6E6E6E(FF) (100% opacity).
 */
object PluginIcons {
    val ACCENT_COLOR: Int = JBColor(
        Color(255, 0, 144),
        Color(255, 0, 144)
    ).rgb
    val A_PLUS_MODULE: Icon = getIcon("/META-INF/icons/module.svg", PluginIcons::class.java)
    val A_PLUS_MODULE_DISABLED: Icon = getIcon("/META-INF/icons/moduleDisabled.svg", PluginIcons::class.java)
    val A_PLUS_EXERCISE_GROUP: Icon = getIcon("/META-INF/icons/exerciseGroup.svg", PluginIcons::class.java)
    val A_PLUS_EXERCISE_GROUP_CLOSED: Icon =
        getIcon("/META-INF/icons/exerciseGroupClosed.svg", PluginIcons::class.java)
    val A_PLUS_OPTIONAL_PRACTICE: Icon = getIcon("/META-INF/icons/optionalPractice.svg", PluginIcons::class.java)
    val A_PLUS_NO_SUBMISSIONS: Icon = getIcon("/META-INF/icons/noSubmissions.svg", PluginIcons::class.java)
    val A_PLUS_NO_POINTS: Icon = getIcon("/META-INF/icons/noPoints.svg", PluginIcons::class.java)
    val A_PLUS_PARTIAL_POINTS: Icon = getIcon("/META-INF/icons/partialPoints.svg", PluginIcons::class.java)
    val A_PLUS_FULL_POINTS: Icon = getIcon("/META-INF/icons/fullPoints.svg", PluginIcons::class.java)
    val A_PLUS_LATE: Icon = getIcon("/META-INF/icons/late.svg", PluginIcons::class.java)
    val A_PLUS_REPL: Icon = getIcon("/META-INF/icons/repl.svg", PluginIcons::class.java)
    val A_PLUS_COURSES_BANNER: Icon = getIcon("/META-INF/images/courses-banner.png", PluginIcons::class.java)
    val A_PLUS_COURSES_FOOTER: Icon = getIcon("/META-INF/images/footer.png", PluginIcons::class.java)
    val A_PLUS_USER: Icon = getIcon("/META-INF/icons/user.svg", PluginIcons::class.java)
    val A_PLUS_USER_ACTIVE: Icon = getIcon("/META-INF/icons/user_pink.svg", PluginIcons::class.java)
    val A_PLUS_INFO: Icon = getIcon("/META-INF/icons/info.svg", PluginIcons::class.java)
    val A_PLUS_DUMMY: Icon = getIcon("/META-INF/icons/dummy.svg", PluginIcons::class.java)
    val A_PLUS_CHECKED: Icon = getIcon("/META-INF/icons/checked.svg", PluginIcons::class.java)
    val A_PLUS_DOCS: Icon = getIcon("/META-INF/icons/docs.svg", PluginIcons::class.java)
    val A_PLUS_NEW: Icon = getIcon("/META-INF/icons/new.svg", PluginIcons::class.java)
    val A_PLUS_LOADING: Icon = AnimatedIcon.Default()
    val A_PLUS_PLUS: Icon = AllIcons.General.Add

    // Icons in plugin.xml need the @JvmField annotation
    @JvmField
    val A_PLUS_DOWNLOAD: Icon = getIcon("/META-INF/icons/download.svg", PluginIcons::class.java)

    @JvmField
    val A_PLUS_UPLOAD: Icon = getIcon("/META-INF/icons/upload.svg", PluginIcons::class.java)

    @JvmField
    val A_PLUS_BROWSE: Icon = getIcon("/META-INF/icons/web.svg", PluginIcons::class.java)

    @JvmField
    val A_PLUS_FEEDBACK: Icon = getIcon("/META-INF/icons/feedback.svg", PluginIcons::class.java)

    @JvmField
    val A_PLUS_REFRESH: Icon = getIcon("/META-INF/icons/refresh.svg", PluginIcons::class.java)

    @JvmField
    val A_PLUS_FILTER: Icon = getIcon("/META-INF/icons/filter.svg", PluginIcons::class.java)

    @JvmField
    val A_PLUS_LOGO: Icon = getIcon("/META-INF/icons/aPlusLogo.svg", PluginIcons::class.java)

    @JvmField
    val A_PLUS_LOGO_COLOR: Icon = getIcon("/META-INF/icons/aPlusLogoColor.svg", PluginIcons::class.java)
}
