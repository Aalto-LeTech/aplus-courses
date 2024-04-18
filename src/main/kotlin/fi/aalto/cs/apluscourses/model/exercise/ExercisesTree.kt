package fi.aalto.cs.apluscourses.model.exercise

class ExercisesTree(
    val exerciseGroups: MutableList<ExerciseGroup> = emptyList<ExerciseGroup>().toMutableList(),
//    selectedStudent: Student? = null
) {
//    private val selectedStudent: Student? = selectedStudent
//
//    fun getSelectedStudent(): Student? {
//        return selectedStudent
//    }

    /**
     * Finds the exercise from the given url, returns null if not found.
     */
    fun findExerciseByUrl(htmlUrl: String): Exercise? {
        return exerciseGroups.stream()
            .flatMap { group: ExerciseGroup -> group.exercises.stream() }
            .filter { exercise: Exercise? -> exercise!!.htmlUrl == htmlUrl }
            .findFirst()
            .orElse(null)
    }
}
