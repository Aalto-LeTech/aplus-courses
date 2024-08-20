package fi.aalto.cs.apluscourses.model.people

/**
 * @property userName The name of the user. If the full name is not available, the username is used.
 * @property studentId The student ID of the user.
 * @property aplusId The A+ ID of the user.
 * @property staffCourses List of course IDs that the user is a staff member of.
 */
class User(
    val userName: String,
    val studentId: String,
    val aplusId: Long,
    val enrolledCourses: List<Long>,
    val staffCourses: List<Long>,
) {
    fun isStaffOf(courseId: Long): Boolean {
        return courseId in staffCourses
    }

    fun isEnrolledIn(courseId: Long): Boolean {
        return courseId in enrolledCourses
    }
}
