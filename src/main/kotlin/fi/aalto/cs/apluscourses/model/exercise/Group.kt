package fi.aalto.cs.apluscourses.model.exercise

import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.model.exercise.Group.GroupMember

// TODO remove?
data class Group(val id: Long, val members: List<GroupMember>) {
    class GroupMember(val id: Long, val name: String)

    val memberNames: List<String> = members.map { it.name }

    /**
     * Returns an identifier for the group, based on the members of the group. The difference between this and
     * [getId][.getId] is that this method will return the same ID for two different groups if their
     * members are the same.
     */ // TODO remove?
    val memberwiseId: String = members.sortedBy { it.id }.joinToString(", ") { it.name }

    override fun toString(): String = memberwiseId

    companion object {
        val GROUP_ALONE: Group = Group(
            -1,
            listOf(
                GroupMember(
                    -1,
                    message("ui.toolWindow.subTab.exercises.submission.submitAlone")
                )
            )
        )
    }
}
