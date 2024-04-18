package fi.aalto.cs.apluscourses.services.exercise

import com.intellij.openapi.components.Service
import fi.aalto.cs.apluscourses.model.exercise.Exercise

@Service(Service.Level.PROJECT)
class SelectedExerciseService {
    var selectedExercise: Exercise? = null
}