package fi.aalto.cs.apluscourses.services.exercise

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.PROJECT)
class SelectedExerciseService(
    private val project: Project,
    private val cs: CoroutineScope
) {
    var selectedExercise: Exercise? = null
    var selectedExerciseTreeItem: ExercisesView.ExercisesTreeItem? = null
}