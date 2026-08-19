package fi.aalto.cs.apluscourses.generator

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.TestDisposable
import com.intellij.testFramework.junit5.fixture.projectFixture
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import kotlin.io.path.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test

@TestApplication
class APlusModuleBuilderTest {
    private val projectFixture = projectFixture()

    @TestDisposable
    private lateinit var disposable: Disposable

    private fun builderFor(basePath: String): APlusModuleBuilder {
        val builder = APlusModuleBuilder()
        builder.name = MODULE_NAME
        builder.contentEntryPath = basePath
        builder.moduleFilePath = Path(basePath).resolve("$MODULE_NAME.iml").toString()
        builder.config.language = LANGUAGE
        builder.config.courseConfigUrl = COURSE_CONFIG_URL
        builder.config.importSettings = true
        builder.config.programmingLanguage = PROGRAMMING_LANGUAGE
        return builder
    }

    @Test
    fun `commit creates a module and stores the course settings`(): Unit = runBlocking(Dispatchers.EDT) {
        val project = projectFixture.get()
        val builder = builderFor(requireNotNull(project.basePath))

        val modules = builder.commit(project, null, null)

        assertEquals(1, modules.size)
        assertTrue(ModuleManager.getInstance(project).modules.any { it.name == "TestCourse" })

        val state = CourseFileManager.getInstance(project).state
        assertEquals("en", state.language)
        assertEquals("https://example.org/course.json", state.url)
        assertTrue(state.importSettings)
    }

    @Test
    fun `the created project has no SDK when the wizard did not select one`(): Unit = runBlocking(Dispatchers.EDT) {
        val project = projectFixture.get()
        val builder = builderFor(requireNotNull(project.basePath))

        builder.commit(project, null, null)

        assertNull(ProjectRootManager.getInstance(project).projectSdk)
    }

    @Test
    fun `the JDK chosen in the wizard becomes the project SDK`(): Unit = runBlocking(Dispatchers.EDT) {
        val project = projectFixture.get()
        val jdk = ProjectJdkTable.getInstance().createSdk(JDK_NAME, JavaSdk.getInstance())
        runWriteAction {
            jdk.sdkModificator.apply {
                homePath = System.getProperty("java.home")
                commitChanges()
            }
            ProjectJdkTable.getInstance().addJdk(jdk, disposable)
        }

        val builder = builderFor(requireNotNull(project.basePath))
        builder.config.jdk = jdk

        builder.commit(project, null, null)

        assertEquals(jdk, ProjectRootManager.getInstance(project).projectSdk)
    }

    private companion object {
        @NonNls
        const val PROGRAMMING_LANGUAGE = "python"

        @NonNls
        const val MODULE_NAME = "TestCourse"

        @NonNls
        const val LANGUAGE = "en"

        @NonNls
        const val COURSE_CONFIG_URL = "https://example.org/course.json"

        @NonNls
        const val JDK_NAME = "test-jdk"
    }
}
