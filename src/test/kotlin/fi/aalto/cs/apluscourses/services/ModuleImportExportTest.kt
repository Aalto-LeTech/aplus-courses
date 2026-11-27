package fi.aalto.cs.apluscourses.services

import com.intellij.testFramework.junit5.TestApplication
import fi.aalto.cs.apluscourses.model.people.Group
import fi.aalto.cs.apluscourses.model.people.User
import fi.aalto.cs.apluscourses.services.ModuleImportExport.Companion.createSubmittersInfo
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class ModuleImportExportTest {
    @Test
    fun `exporting alone lists only the submitter`() {
        val info = createSubmittersInfo(student(), Group.EXPORT_ALONE)

        assertEquals(1, info.trim().lines().size, info)
        assertTrue(info.contains(STUDENT_NAME), info)
        assertTrue(info.contains(STUDENT_ID), info)
    }

    @Test
    fun `submitting alone lists only the submitter too`() {
        val info = createSubmittersInfo(student(), Group.SUBMIT_ALONE)

        assertEquals(1, info.trim().lines().size, info)
    }

    @Test
    fun `a real group lists its members`() {
        val group = Group(
            7,
            listOf(Group.GroupMember(1, "Ada Lovelace"), Group.GroupMember(2, "Grace Hopper"))
        )

        val info = createSubmittersInfo(student(), group)

        assertTrue(info.contains("- Ada Lovelace"), info)
        assertTrue(info.contains("- Grace Hopper"), info)
    }

    private fun student() = User(
        userName = STUDENT_NAME,
        studentId = STUDENT_ID,
        aplusId = 1,
        enrolledCourses = emptyList(),
        staffCourses = emptyList()
    )

    private companion object {
        @NonNls
        const val STUDENT_NAME = "Alan Turing"

        @NonNls
        const val STUDENT_ID = "123456"
    }
}
