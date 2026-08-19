package fi.aalto.cs.apluscourses.generator

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.TestDisposable
import com.intellij.testFramework.junit5.fixture.projectFixture
import fi.aalto.cs.apluscourses.api.CourseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test

@TestApplication
class CourseSettingsStepTest {
    private val projectFixture = projectFixture()

    @TestDisposable
    private lateinit var disposable: Disposable

    private fun courseConfig(vararg languages: String) = CourseConfig.JSON(
        id = "1",
        name = COURSE_NAME,
        aPlusUrl = "https://plus.cs.aalto.fi/",
        languages = languages.toList(),
        modules = emptyList(),
        exerciseModules = emptyMap()
    )

    private fun step(
        programmingLanguage: String,
        courseJson: CourseConfig.JSON
    ): Pair<CourseSettingsStep, APlusModuleConfig> {
        val builder = APlusModuleBuilder()
        builder.config.programmingLanguage = programmingLanguage
        builder.config.courseConfig = courseJson
        val step = CourseSettingsStep(
            WizardContext(projectFixture.get(), disposable),
            builder,
            disposable,
            builder.config
        )
        return step to builder.config
    }

    @Test
    fun `a rebuilt scala step publishes the panel it just built`(): Unit = runBlocking(Dispatchers.EDT) {
        val (step, _) = step(SCALA, courseConfig(ENGLISH, FINNISH))

        step.updateStep()
        val first = step.component
        step.updateStep()

        assertNotSame(
            first,
            step.component,
            "updateStep builds a new panel, and getComponent has to hand back that one"
        )
    }

    @Test
    fun `a rebuilt step still writes its selections into the config`(): Unit = runBlocking(Dispatchers.EDT) {
        val courseJson = courseConfig(ENGLISH, FINNISH)
        val (step, moduleConfig) = step(SCALA, courseJson)

        step.updateStep()
        step.updateStep()
        step.updateDataModel()

        assertTrue(
            moduleConfig.language in courseJson.languages,
            "expected one of ${courseJson.languages}, got: ${moduleConfig.language}"
        )
    }

    @Test
    fun `a non-scala course step can be shown twice`(): Unit = runBlocking(Dispatchers.EDT) {
        val (step, moduleConfig) = step(PYTHON, courseConfig(ENGLISH))

        step.updateStep()
        step.updateStep()
        step.updateDataModel()

        assertEquals(ENGLISH, moduleConfig.language)
    }

    private companion object {
        @NonNls
        const val COURSE_NAME = "Test Course"

        @NonNls
        const val SCALA = "scala"

        @NonNls
        const val PYTHON = "python"

        @NonNls
        const val ENGLISH = "en"

        @NonNls
        const val FINNISH = "fi"
    }
}
