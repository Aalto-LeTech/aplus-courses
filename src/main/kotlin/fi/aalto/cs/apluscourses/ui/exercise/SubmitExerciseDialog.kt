package fi.aalto.cs.apluscourses.ui.exercise

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.ui.RoundedLineBorder
import com.intellij.ui.dsl.builder.panel
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.ui.FileRenderer
import fi.aalto.cs.apluscourses.ui.FileTree
import java.nio.file.Path
import javax.swing.BorderFactory
import kotlin.io.path.name

class SubmitExerciseDialog(val project: Project, val exercise: Exercise, val files: List<Path>) :
    DialogWrapper(project) {
    init {
        setOKButtonText("Submit")
        title = "Submit ${exercise.name}"
        setSize(0, 0)
        init()
    }

    // TODO check if can submit with group (if has submitted with group before)

    override fun createCenterPanel(): DialogPanel = panel {
//        row {
//            label(exercise.name).bold()
//        }
        val submissionNumber = exercise.submissionResults.size + 1

        row {
            label("Files to submit:")
        }
        row {
            cell(FileTree(files, project)).applyToComponent {
                cellRenderer = FileRenderer(files.associate { it.name to it })
                isEnabled = false
            }
        }
        row("Group:") {
            comboBox(listOf("Submit alone", "TODO", "Group 1", "Group 2"))
            button(
                "Set as default"
            ) {}
        }
        row {
            text("You are about to make submission ${submissionNumber} out of ${exercise.maxSubmissions}.")
        }
        row {
            if (exercise.maxSubmissions <= (submissionNumber)) {
                text(
                    if (submissionNumber == exercise.maxSubmissions) {
                        MyBundle.message("presentation.submissionViewModel.warning.lastSubmission")
                    } else {
                        MyBundle.message("presentation.submissionViewModel.warning.submissionsExceeded")
                    }
                ).applyToComponent {
                    foreground = JBColor.namedColor("Notification.ToolWindow.errorForeground")
                    background = JBColor.namedColor("Notification.ToolWindow.errorBackground")
                    isOpaque = true
                    val errorBorderColor = JBColor.namedColor("Notification.ToolWindow.errorBorderColor")

                    border = BorderFactory.createCompoundBorder(
                        RoundedLineBorder(errorBorderColor, 10, 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                    )
                }
            }
        }
    }

}