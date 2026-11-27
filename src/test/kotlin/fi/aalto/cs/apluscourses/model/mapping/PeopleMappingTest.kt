package fi.aalto.cs.apluscourses.model.mapping

import fi.aalto.cs.aplus.client.dto.CourseBriefDto
import fi.aalto.cs.aplus.client.dto.GroupDto
import fi.aalto.cs.aplus.client.dto.GroupMemberDto
import fi.aalto.cs.aplus.client.dto.UserDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PeopleMappingTest {
    @Test
    fun `a member's user id comes from the member URL, not from the member id`() {
        val groups = toGroups(
            listOf(
                GroupDto(
                    id = 7,
                    members = listOf(
                        GroupMemberDto(id = 1, url = "https://plus.cs.aalto.fi/api/v2/users/321/", fullName = "Ada")
                    )
                )
            )
        )

        assertEquals(321, groups.single().members.single().id, "the member id is not the A+ user id")
    }

    @Test
    fun `a member URL without a trailing slash is read the same way`() {
        val groups = toGroups(
            listOf(GroupDto(id = 7, members = listOf(GroupMemberDto(id = 1, url = "/api/v2/users/99"))))
        )

        assertEquals(99, groups.single().members.single().id)
    }

    @Test
    fun `a member without a URL is left out rather than given the member id`() {
        val groups = toGroups(listOf(GroupDto(id = 7, members = listOf(GroupMemberDto(id = 5, url = null)))))

        assertTrue(groups.single().members.isEmpty())
    }

    @Test
    fun `a member without a name gets an empty one rather than failing`() {
        val groups = toGroups(listOf(GroupDto(id = 7, members = listOf(GroupMemberDto(id = 5, url = "users/50/", fullName = null)))))

        assertEquals("", groups.single().members.single().name)
    }

    @Test
    fun `the group id and every member are kept`() {
        val groups = toGroups(
            listOf(
                GroupDto(
                    id = 12,
                    members = listOf(
                        GroupMemberDto(id = 1, url = "users/10/", fullName = "Ada"),
                        GroupMemberDto(id = 2, url = "users/20/", fullName = "Grace")
                    )
                )
            )
        )

        val group = groups.single()
        assertEquals(12, group.id)
        assertEquals(listOf(10L, 20L), group.members.map { it.id })
        assertEquals(listOf("Ada", "Grace"), group.members.map { it.name })
    }

    @Test
    fun `no groups maps to no groups`() {
        assertTrue(toGroups(emptyList()).isEmpty())
    }

    @Test
    fun `the full name is preferred over the username`() {
        val user = toUser(UserDto(id = 1, username = "adal1", isExternal = false, fullName = "Ada Lovelace"))

        assertEquals("Ada Lovelace", user.userName)
    }

    @Test
    fun `a missing or blank full name falls back to the username`() {
        assertEquals("adal1", toUser(UserDto(id = 1, username = "adal1", isExternal = false, fullName = null)).userName)
        assertEquals("adal1", toUser(UserDto(id = 1, username = "adal1", isExternal = false, fullName = " ")).userName)
    }

    @Test
    fun `enrolled and staff courses become plain id lists`() {
        val user = toUser(
            UserDto(
                id = 42,
                username = "adal1",
                studentId = "123456",
                isExternal = false,
                enrolledCourses = listOf(CourseBriefDto(id = 1), CourseBriefDto(id = 2)),
                staffCourses = listOf(CourseBriefDto(id = 3))
            )
        )

        assertEquals(42, user.aplusId)
        assertEquals("123456", user.studentId)
        assertEquals(listOf(1L, 2L), user.enrolledCourses)
        assertEquals(listOf(3L), user.staffCourses)
        assertTrue(user.isEnrolledIn(1))
        assertTrue(user.isStaffOf(3))
    }

    @Test
    fun `a user with no student id keeps it null`() {
        assertNull(toUser(UserDto(id = 1, username = "adal1", isExternal = false)).studentId)
    }
}
