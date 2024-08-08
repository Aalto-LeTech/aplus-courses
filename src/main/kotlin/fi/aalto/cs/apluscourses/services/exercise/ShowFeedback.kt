package fi.aalto.cs.apluscourses.services.exercise

import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider.Companion.openEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.writeText
import com.intellij.ui.JBColor
import com.intellij.util.application
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.StartupUiUtil
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.api.APlusApi
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult
import fi.aalto.cs.apluscourses.services.course.CourseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                val feedbackCss = CourseManager.getInstance(project).state.feedbackCss ?: return@launch
                if (feedbackCss.isEmpty()) {
                    return@launch
                }
                val feedbackString = withContext(Dispatchers.IO) {
                    APlusApi.submission(submission).get(project).feedback
                }

                val document = Jsoup.parseBodyFragment(feedbackString)

                val textColor: JBColor = JBColor.black
                val textColorString =
                    String.format("#%02x%02x%02x", textColor.red, textColor.green, textColor.blue)
                val backgroundColor: Color = JBColor.background()
                val backgroundColorString = String.format(
                    "#%02x%02x%02x", backgroundColor.red, backgroundColor.green,
                    backgroundColor.blue
                )

                val fontName: String = JBFont.regular().fontName

                println(feedbackCss)

                document.head().append(
                    """<script src="https://code.jquery.com/jquery-3.7.0.min.js" integrity="sha256-2Pmvv0kuTBOenSvLm6bvfBSSHrUJ+3A7x6P5Ebd07/g=" crossorigin="anonymous" referrerpolicy="no-referrer"></script>"""
                )

                document.head().append(
                    """<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap3/dist/css/bootstrap.min.css"/>"""
                )

                document.head().append(
                    "<style>"
                            + Jsoup.clean(
                        feedbackCss
                            .replace("TEXT_COLOR", textColorString)
                            .replace("BG_COLOR", backgroundColorString)
                            .replace("FONT_NAME", fontName),
                        Safelist.none()
                    )
                            + "</style>"
                )

                if (!JBColor.isBright()) {
                    document.body().addClass("dark")
                }

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
//            notifier.notify(NetworkErrorNotification(ex), project)
            }
        }
    }

    fun updateBrowserTitle(file: VirtualFile, newTitle: String) {
//        cs.launch {
//            writeCommandAction(project, "Change Page Title") {
        application.runWriteAction {
            file.writeText(newTitle)
            FileEditorManager.getInstance(project).updateFilePresentation(file)
        }
    }
}