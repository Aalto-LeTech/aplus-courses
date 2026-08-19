package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.roots.ModuleOrderEntry
import com.intellij.openapi.roots.ui.configuration.actions.ModuleDeleteProvider
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.TestDisposable
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import fi.aalto.cs.apluscourses.model.Course
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.utils.Version
import fi.aalto.cs.apluscourses.utils.callbacks.Callbacks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test

/**
 * A removed A+ module must stay in its dependants' dependency lists, so that re-downloading it
 * restores a working dependency instead of leaving the dependant silently unlinked.
 */
@TestApplication
class ModuleDependencyPreserverTest {
    private val projectFixture = projectFixture()
    private val libraryModuleFixture = projectFixture.moduleFixture("O1Library")
    private val dependantModuleFixture = projectFixture.moduleFixture(DEPENDANT_MODULE)

    @TestDisposable
    private lateinit var disposable: Disposable

    private fun markAsCourseProject(project: Project) {
        CourseManager.getInstance(project).state.course = Course(
            id = 1,
            name = COURSE_NAME,
            languages = listOf(LANGUAGE),
            modules = emptyList(),
            exerciseModules = emptyMap(),
            resourceUrls = emptyMap(),
            optionalCategories = emptyList(),
            autoInstallComponentNames = emptyList(),
            replInitialCommands = null,
            replAdditionalArguments = null,
            minimumPluginVersion = Version.DEFAULT,
            hiddenElements = emptyList(),
            callbacks = Callbacks.fromJsonObject(null),
            project = project
        )
    }

    @Test
    fun `removing a module keeps it as a dependency of the modules that depended on it`(): Unit =
        runBlocking(Dispatchers.EDT) {
            val project = projectFixture.get()
            val library = libraryModuleFixture.get()
            val dependant = dependantModuleFixture.get()
            markAsCourseProject(project)

            runWriteAction {
                val model = dependant.rootManager.modifiableModel
                model.addModuleOrderEntry(library)
                model.commit()
            }

            project.messageBus.connect(disposable)
                .subscribe(ModuleListener.TOPIC, ModuleDependencyPreserver())

            runWriteAction {
                ModuleDeleteProvider.detachModules(project, arrayOf(library))
            }

            val entries = dependant.rootManager.orderEntries
                .filterIsInstance<ModuleOrderEntry>()
                .map { it.moduleName }
            assertTrue(
                entries.contains("O1Library"),
                "expected the dependency to survive removal, got: $entries"
            )
            assertNotNull(project)
        }

    private companion object {
        @NonNls
        const val DEPENDANT_MODULE = "Goodstuff"

        @NonNls
        const val COURSE_NAME = "Test Course"

        @NonNls
        const val LANGUAGE = "en"
    }
}
