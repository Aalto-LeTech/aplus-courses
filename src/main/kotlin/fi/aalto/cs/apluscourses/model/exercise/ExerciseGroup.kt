package fi.aalto.cs.apluscourses.model.exercise

data class ExerciseGroup(
    val id: Long,
    val name: String,
    val maxPoints: Int,
    val userPoints: Int,
    val htmlUrl: String,
    val isOpen: Boolean,
    val closingTime: String?,
    val exercises: MutableList<Exercise>
)
