package fi.aalto.cs.apluscourses.ui.exercise

import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.properties.whenPropertyChanged
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.RoundedLineBorder
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.util.ui.JBUI.CurrentTheme.NotificationError
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.people.Group
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.ui.FileRenderer
import fi.aalto.cs.apluscourses.ui.FileTree
import java.nio.file.Path
import javax.swing.BorderFactory
import kotlin.io.path.name

class SubmitExerciseDialog(
    val project: Project,
    val exercise: Exercise,
    val files: List<Path>,
    private val groups: List<Group>,
    val group: Group,
    private val submittedBefore: Boolean,
) :
    DialogWrapper(project) {
    val selectedGroup: AtomicProperty<Group> = AtomicProperty<Group>(group)
    val defaultGroup: AtomicProperty<Group> = AtomicProperty<Group>(group)
    private val isDefaultSelected = object : ComponentPredicate() {
        override fun invoke(): Boolean {
            return selectedGroup.get() != defaultGroup.get()
        }

        override fun addListener(listener: (Boolean) -> Unit) {
            selectedGroup.whenPropertyChanged { listener(it != defaultGroup.get()) }
            defaultGroup.whenPropertyChanged { listener(selectedGroup.get() != it) }
        }
    }

    init {
        setOKButtonText(message("ui.SubmitExerciseDialog.okButton"))
        title = message("ui.SubmitExerciseDialog.title", exercise.name)
        setSize(0, 0)
        init()
    }

    override fun createCenterPanel(): DialogPanel = panel {
        val submissionNumber = exercise.submissionResults.size + 1
        val showGroups = project.service<CourseManager>().state.alwaysShowGroups || groups.size > 1

        row {
            label(message("ui.SubmitExerciseDialog.files"))
        }
        row {
            cell(FileTree(files, project)).applyToComponent {
                cellRenderer = FileRenderer(files.associateBy { it.name })
                isEnabled = false
            }
        }
        if (showGroups) {
            row(message("ui.SubmitExerciseDialog.group")) {
                comboBox(groups).bindItem(selectedGroup).enabled(!submittedBefore)
                button(
                    message("ui.SubmitExerciseDialog.groupDefaultButton")
                ) {
                    defaultGroup.set(selectedGroup.get())
                    CourseFileManager.getInstance(project).setDefaultGroup(selectedGroup.get())
                }
                    .enabled(!submittedBefore)
                    .enabledIf(isDefaultSelected)
                contextHelp(message("ui.SubmitExerciseDialog.cantChangeGroup"))
                    .visible(submittedBefore)
            }
        }
        row {
            text(
                if (exercise.maxSubmissions < 1) message(
                    "ui.SubmitExerciseDialog.descriptionOptional",
                    submissionNumber
                ) else message(
                    "ui.SubmitExerciseDialog.description",
                    submissionNumber,
                    exercise.maxSubmissions
                )
            )
        }
        row {
            if (!exercise.isOptional && submissionNumber >= exercise.maxSubmissions) {
                text(
                    if (submissionNumber == exercise.maxSubmissions) message("ui.SubmitExerciseDialog.warning.lastSubmission")
                    else message("ui.SubmitExerciseDialog.warning.submissionsExceeded")
                ).applyToComponent {
                    foreground = NotificationError.foregroundColor()
                    background = NotificationError.backgroundColor()
                    isOpaque = true
                    val errorBorderColor = NotificationError.borderColor()

                    border = BorderFactory.createCompoundBorder(
                        RoundedLineBorder(errorBorderColor, 10, 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                    )
                }
            }
        }
    }

}