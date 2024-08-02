package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.model.component.old.OldModule
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.PROJECT)
class SelectedItems(
    private val project: Project,
    val cs: CoroutineScope
) {
    var selectedModule: OldModule? = null
    var selectedExerciseItem: ExercisesView.ExerciseItem? = null
}