package fi.aalto.cs.apluscourses.services.course

import com.intellij.openapi.project.Project
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.util.xmlb.XmlSerializer
import com.intellij.testFramework.junit5.fixture.projectFixture
import fi.aalto.cs.apluscourses.utils.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertNull
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@TestApplication
class CourseFileManagerTest {
    private val projectFixture = projectFixture()

    private fun writeLegacyConfig(project: Project, @NonNls json: String) {
        val ideaDir = Path(requireNotNull(project.basePath)).resolve(Project.DIRECTORY_STORE_FOLDER)
        ideaDir.createDirectories()
        ideaDir.resolve("a-plus-project.json").writeText(json)
    }

    @Test
    fun `migrates a legacy a-plus-project json into the persistent state`() {
        val project = projectFixture.get()
        writeLegacyConfig(
            project,
            """
            {
              "language": "en",
              "url": "https://plus.cs.aalto.fi/o1/2024/",
              "modules": {
                "Goodstuff": { "downloadedAt": "2024-09-01T12:00:00Z", "version": "1.4" }
              }
            }
            """.trimIndent()
        )

        val manager = CourseFileManager.getInstance(project)
        manager.migrateOldConfig()

        assertEquals("en", manager.state.language)
        assertEquals("https://plus.cs.aalto.fi/o1/2024/", manager.state.url)

        val module = manager.getMetadata(MODULE_NAME)
        assertEquals(Version("1.4"), module?.version)
        assertEquals(1725192000L, module?.downloadedAt?.epochSeconds)
    }

    @Test
    fun `whether the grade section applies survives serialization`() {
        val state = CourseFileManager.getInstance(projectFixture.get()).state

        assertFalse(state.showsGradeProgression, "nothing is known before the first load")

        val modificationsBefore = state.modificationCount
        state.showsGradeProgression = true

        assertTrue(
            state.modificationCount > modificationsBefore,
            "setting the flag has to mark the state modified, or it is never written out"
        )

        val restored = XmlSerializer.deserialize(
            XmlSerializer.serialize(state),
            CourseFileManager.State::class.java
        )
        assertTrue(restored.showsGradeProgression, "the flag should be written to disk and read back")
    }

    @Test
    fun `migration is a no-op when no legacy config is present`() {
        val project = projectFixture.get()

        val manager = CourseFileManager.getInstance(project)
        manager.migrateOldConfig()

        assertNull(manager.state.url)
        assertEquals(0, manager.state.modules.size)
    }

    private companion object {
        @NonNls
        const val MODULE_NAME = "Goodstuff"
    }
}
