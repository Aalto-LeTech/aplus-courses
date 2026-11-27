package fi.aalto.cs.apluscourses.model.mapping

import fi.aalto.cs.apluscourses.model.people.Group
import fi.aalto.cs.apluscourses.model.people.User
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import fi.aalto.cs.aplus.client.dto.GroupDto
import fi.aalto.cs.aplus.client.dto.GroupMemberDto
import fi.aalto.cs.aplus.client.dto.UserDto

/**
 * Maps `mygroups` members to plugin users.
 *
 * A+ assigns separate IDs to group memberships and users.
 * [GroupMemberDto.userIdOrNull] extracts the user ID from the member URL.
 * Members without a readable user URL are logged and omitted.
 */
fun toGroups(groups: List<GroupDto>): List<Group> = groups.map { group ->
    Group(
        group.id,
        group.members.mapNotNull { member ->
            val userId = member.userIdOrNull
            if (userId == null) {
                CoursesLogger.warn(
                    "Group ${group.id} member ${member.id} has no readable user URL (\"${member.url}\"), " +
                        "leaving the member out"
                )
                return@mapNotNull null
            }
            Group.GroupMember(userId, member.fullName.orEmpty())
        }
    )
}

/** Maps `users/me`, falling back to the username when A+ knows no full name. */
fun toUser(user: UserDto): User = User(
    user.displayName,
    user.studentId,
    user.id,
    user.enrolledCourses.map { it.id },
    user.staffCourses.map { it.id }
)
