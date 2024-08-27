package fi.aalto.cs.apluscourses.services.exercise

import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider.Companion.openEditor
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.jcef.JBCefScrollbarsHelper
import com.intellij.util.ui.JBFont
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.api.APlusApi
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult
import fi.aalto.cs.apluscourses.notifications.NetworkErrorNotification
import fi.aalto.cs.apluscourses.services.Notifier
import fi.aalto.cs.apluscourses.services.Opener
import fi.aalto.cs.apluscourses.services.course.CourseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.NonNls
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import java.awt.Color
import java.io.IOException

@Service(Service.Level.PROJECT)
class ShowFeedback(
    private val project: Project,
    private val cs: CoroutineScope
) {
    fun showFeedback(submission: SubmissionResult, exercise: Exercise) {
        cs.launch {
            try {
                val feedbackCss = CourseManager.getInstance(project).state.feedbackCss ?: ""
                if (feedbackCss.isEmpty() || !exercise.isSubmittable) {
                    project.service<Opener>().openSubmission(submission)
                    return@launch
                }
                val feedbackString = withContext(Dispatchers.IO) {
                    APlusApi.submission(submission).get(project).feedback
                }

                val document = Jsoup.parseBodyFragment(feedbackString)

                @NonNls val rgbFormat = "#%02x%02x%02x"
                val textColor: JBColor = JBColor.black
                val textColorString =
                    String.format(rgbFormat, textColor.red, textColor.green, textColor.blue)

                val backgroundColor: Color = JBColor.background()
                val backgroundColorString =
                    String.format(rgbFormat, backgroundColor.red, backgroundColor.green, backgroundColor.blue)

                val fontName: String = JBFont.regular().fontName

                @NonNls val jquery =
                    """<script src="https://code.jquery.com/jquery-3.7.0.min.js" integrity="sha256-2Pmvv0kuTBOenSvLm6bvfBSSHrUJ+3A7x6P5Ebd07/g=" crossorigin="anonymous" referrerpolicy="no-referrer"></script>"""
                @NonNls val bootstrapJs =
                    """<script src="https://cdn.jsdelivr.net/npm/bootstrap@3.4.1/dist/js/bootstrap.min.js"></script>"""
                @NonNls val bootstrapCss =
                    """<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@3.4.1/dist/css/bootstrap.min.css"/>"""

                document.head().append(jquery)
                document.head().append(bootstrapJs)
                document.head().append(bootstrapCss)

                @NonNls val style = "<style>${
                    Jsoup.clean(
                        feedbackCss
                            .replace(Regex("(--bg-color:\\s*)(.*;)"), "$1$backgroundColorString;")
                            .replace(Regex("(--fg-color:\\s*)(.*;)"), "$1$textColorString;")
                            .replace(Regex("(--font-name:\\s*)(.*;)"), "$1\"$fontName\";"),
                        Safelist.none()
                    )
                }${JBCefScrollbarsHelper.buildScrollbarsStyle()}</style>"

                document.head().append(style)

                @NonNls val darkClassName = "dark"

                if (!JBColor.isBright()) document.body().addClass(darkClassName)

                val fileEditorManager: FileEditorManager = FileEditorManager.getInstance(project)

                withContext(Dispatchers.EDT) {
                    // Close all feedback editor tabs
                    fileEditorManager.allEditors.forEach { editor: FileEditor ->
                        if (editor.file != null && editor.file.name.startsWith("Feedback for ")) {
                            fileEditorManager.closeFile(editor.file)
                        }
                    }

                    openEditor(
                        project, message(
                            "ui.ShowFeedbackAction.feedbackTitle",
                            exercise.name, submission.id.toString()
                        ),
                        document.html()
                    )
                }
            } catch (ex: IOException) {
                Notifier.notify(NetworkErrorNotification(ex), project)
            }
        }
    }
}