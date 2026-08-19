package fi.aalto.cs.apluscourses.utils

import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import java.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class RequiredPluginsFileTest {
    private val projectFixture = projectFixture()

    @Test
    fun `the file matches the platform's own format, with the ids sorted`() {
        val content = RequiredPluginsFile.content(listOf(SCALA_ID, REPLACE_ID, THIS_PLUGIN_ID, INSPECTIONS_ID))

        assertEquals(PLATFORM_WRITTEN_FILE, content)
    }

    @Test
    fun `parsing reads back what was written`() {
        val ids = setOf(THIS_PLUGIN_ID, SCALA_ID)

        assertEquals(ids, RequiredPluginsFile.pluginIds(RequiredPluginsFile.content(ids)))
    }

    @Test
    fun `a file the platform wrote parses to its plugin ids`() {
        assertEquals(
            setOf(INSPECTIONS_ID, THIS_PLUGIN_ID, REPLACE_ID, SCALA_ID),
            RequiredPluginsFile.pluginIds(PLATFORM_WRITTEN_FILE)
        )
    }

    @Test
    fun `a document without the component holds no plugins`() {
        @NonNls val otherComponent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project version="4">
              <component name="SomethingElse" />
            </project>
        """.trimIndent()

        assertEquals(emptySet<String>(), RequiredPluginsFile.pluginIds(otherComponent))
    }

    @Test
    fun `writing creates the file under _idea and reading returns the ids`(): Unit =
        runBlocking(Dispatchers.EDT) {
            val project = projectFixture.get()
            val ids = setOf(SCALA_ID, THIS_PLUGIN_ID)

            RequiredPluginsFile.write(project, ids)

            assertEquals(ids, RequiredPluginsFile.read(project))
        }

    /** Rewriting identical content would touch the project file on every course refresh. */
    @Test
    fun `an unchanged list does not rewrite the file`(): Unit = runBlocking(Dispatchers.EDT) {
        val project = projectFixture.get()
        val ids = setOf(SCALA_ID)
        RequiredPluginsFile.write(project, ids)
        val stampAfterFirstWrite = requireNotNull(findFile(project)).modificationStamp

        RequiredPluginsFile.write(project, ids)

        assertEquals(stampAfterFirstWrite, requireNotNull(findFile(project)).modificationStamp)
    }

    private fun findFile(project: Project) = LocalFileSystem.getInstance()
        .refreshAndFindFileByNioFile(
            Path.of(requireNotNull(project.basePath), Project.DIRECTORY_STORE_FOLDER, FILE_NAME)
        )

    private companion object {
        @NonNls
        const val FILE_NAME = "externalDependencies.xml"

        @NonNls
        const val INSPECTIONS_ID = "fi.aalto.cs.inspections"

        @NonNls
        const val THIS_PLUGIN_ID = "fi.aalto.cs.intellij-plugin"

        @NonNls
        const val REPLACE_ID = "fi.aalto.cs.replace"

        @NonNls
        const val SCALA_ID = "org.intellij.scala"

        @NonNls
        const val PLATFORM_WRITTEN_FILE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project version=\"4\">\n" +
                "  <component name=\"ExternalDependencies\">\n" +
                "    <plugin id=\"$INSPECTIONS_ID\" />\n" +
                "    <plugin id=\"$THIS_PLUGIN_ID\" />\n" +
                "    <plugin id=\"$REPLACE_ID\" />\n" +
                "    <plugin id=\"$SCALA_ID\" />\n" +
                "  </component>\n" +
                "</project>"
    }
}
