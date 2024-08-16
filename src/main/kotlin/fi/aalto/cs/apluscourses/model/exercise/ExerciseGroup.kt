package fi.aalto.cs.apluscourses.model.exercise

data class ExerciseGroup(
    val id: Long,
    val name: String,
    val maxPoints: Int,
    var userPoints: Int,
    val htmlUrl: String,
    val isOpen: Boolean,
    private val exerciseOrder: List<Long>,
    val exercises: MutableList<Exercise>
) {
    private fun sort() { // TODO remove or use
        exercises.sortWith(Comparator.comparing { exercise: Exercise -> exerciseOrder.indexOf(exercise.id) })
    }

    /**
     * Adds an exercise or replaces an existing one.
     */
    fun addExercise(exercise: Exercise) { // TODO remove or use
        val oldExercise = exercises.stream().filter { oldEx: Exercise -> oldEx == exercise }.findFirst()
        oldExercise.ifPresent { o: Exercise -> exercises.remove(o) }
        val index = exerciseOrder.indexOf(exercise.id)
        if (index == -1) {
            exercises.add(exercise)
        } else if (index > exercises.size - 1) {
            exercises.add(exercise)
            sort()
        } else {
            exercises.add(index, exercise)
        }
    }
}
