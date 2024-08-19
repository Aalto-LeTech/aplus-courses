package fi.aalto.cs.apluscourses.ui.exercise

import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.properties.whenPropertyChanged
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.ui.RoundedLineBorder
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.ComponentPredicate
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.people.Group
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.ui.FileRenderer
import fi.aalto.cs.apluscourses.ui.FileTree
import java.nio.file.Path
import javax.swing.BorderFactory
import kotlin.io.path.name

class SubmitExerciseDialog(
    val project: Project,
    val exercise: Exercise,
    val files: List<Path>,
    val groups: List<Group>,
    val group: Group,
    val submittedBefore: Boolean,
) :
    DialogWrapper(project) {
    val selectedGroup = AtomicProperty<Group>(group)
    val defaultGroup = AtomicProperty<Group>(group)
    private val test = object : ComponentPredicate() {
        override fun invoke(): Boolean {
            return selectedGroup.get() != defaultGroup.get()
        }

        override fun addListener(listener: (Boolean) -> Unit) {
            selectedGroup.whenPropertyChanged { listener.invoke(it != defaultGroup.get()) }
            defaultGroup.whenPropertyChanged { listener.invoke(selectedGroup.get() != it) }
        }
    }

    init {
        setOKButtonText("Submit")
        title = "Submit ${exercise.name}"
        setSize(0, 0)
        init()
    }

    override fun createCenterPanel(): DialogPanel = panel {
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
            comboBox(groups).bindItem(selectedGroup).enabled(!submittedBefore)
            button(
                "Set as Default"
            ) {
                defaultGroup.set(selectedGroup.get())
                CourseFileManager.getInstance(project).setDefaultGroup(selectedGroup.get())
            }
                .enabled(!submittedBefore)
                .enabledIf(test)
            contextHelp("You cannot change the group after submitting.")
                .visible(submittedBefore)
        }
        row {
            text("You are about to make submission $submissionNumber out of ${exercise.maxSubmissions}.")
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