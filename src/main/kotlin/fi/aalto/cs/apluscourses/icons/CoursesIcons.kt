package fi.aalto.cs.apluscourses.icons

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader.getIcon
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.JBColor
import org.jetbrains.annotations.NonNls
import java.awt.Color
import javax.swing.Icon

/**
 * [Guide](https://plugins.jetbrains.com/docs/intellij/icons.html)
 *
 * [Icons source](https://jetbrains.design/intellij/resources/icons_list/)
 *
 * Accent colour: #FF0090, RGB(255, 0, 144)
 */
object CoursesIcons {
    val AccentColor: JBColor = JBColor(
        Color(255, 0, 144),
        Color(255, 0, 144)
    )

    @NonNls
    private fun icon(path: String): Icon = getIcon(path, CoursesIcons::class.java)

    val Logo: Icon = icon("/icons/aPlusLogo.svg")
    val LogoColor: Icon = icon("/icons/aPlusLogoColor.svg")

    val Module: Icon = icon("/icons/module.svg")
    val ModuleDisabled: Icon = icon("/icons/moduleDisabled.svg")

    val ExerciseGroup: Icon = icon("/icons/exerciseGroup.svg")
    val ExerciseGroupClosed: Icon = icon("/icons/exerciseGroupClosed.svg")
    val OptionalPractice: Icon = icon("/icons/optionalPractice.svg")
    val NoSubmissions: Icon = icon("/icons/noSubmissions.svg")
    val NoPoints: Icon = icon("/icons/noPoints.svg")
    val PartialPoints: Icon = icon("/icons/partialPoints.svg")
    val FullPoints: Icon = icon("/icons/fullPoints.svg")
    val Late: Icon = icon("/icons/late.svg")

    val User: Icon = icon("/icons/user.svg")
    val UserActive: Icon = icon("/icons/user_pink.svg")
    val Info: Icon = icon("/icons/info.svg")
    val Docs: Icon = icon("/icons/docs.svg")
    val NewChip: Icon = icon("/icons/new.svg")
    val Loading: Icon = AnimatedIcon.Default()
    val Plus: Icon = AllIcons.General.Add
    val Download: Icon = icon("/icons/download.svg")
    val Upload: Icon = icon("/icons/upload.svg")
    val Browse: Icon = icon("/icons/web.svg")
    val Feedback: Icon = icon("/icons/feedback.svg")
    val Refresh: Icon = icon("/icons/refresh.svg")
    val Filter: Icon = icon("/icons/filter.svg")

    object About {
        val Banner: Icon = icon("/images/courses-banner.png")
        val Footer: Icon = icon("/images/footer.png")
    }
}
