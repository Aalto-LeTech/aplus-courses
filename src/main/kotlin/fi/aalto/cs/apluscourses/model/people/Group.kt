package fi.aalto.cs.apluscourses.model.people

import fi.aalto.cs.apluscourses.MyBundle.message

data class Group(val id: Long, val members: List<GroupMember>) {
    class GroupMember(val id: Long, val name: String)

    override fun toString(): String = members.sortedBy { it.id }.joinToString(", ") { it.name }

    companion object {
        val SUBMIT_ALONE: Group = Group(
            -1,
            listOf(
                GroupMember(
                    -1,
                    message("people.group.submitAlone")
                )
            )
        )
        val EXPORT_ALONE: Group = Group(
            -1,
            listOf(
                GroupMember(
                    -1,
                    message("ui.ExportModuleDialog.group.exportAlone")
                )
            )
        )
    }
}
