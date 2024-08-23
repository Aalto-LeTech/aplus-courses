package fi.aalto.cs.apluscourses.model.people

import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.model.people.Group.GroupMember

data class Group(val id: Long, val members: List<GroupMember>) {
    class GroupMember(val id: Long, val name: String)

    override fun toString(): String = members.sortedBy { it.id }.joinToString(", ") { it.name }

    companion object {
        val GROUP_ALONE: Group = Group(
            -1,
            listOf(
                GroupMember(
                    -1,
                    message("people.group.submitAlone")
                )
            )
        )
    }
}
