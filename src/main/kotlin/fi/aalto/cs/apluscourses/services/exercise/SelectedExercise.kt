package fi.aalto.cs.apluscourses.services.exercise

import com.intellij.openapi.components.Service
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView

@Service(Service.Level.PROJECT)
class SelectedExercise() {
    var selectedExercise: Exercise? = null
    var selectedExerciseTreeItem: ExercisesView.ExercisesTreeItem? = null
}