package fi.aalto.cs.apluscourses.model.people

import fi.aalto.cs.apluscourses.model.Course

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
    private val staffCourses: List<Long>,
) {
    fun isStaffOf(course: Course): Boolean {
        return course.id in staffCourses
    }
}
