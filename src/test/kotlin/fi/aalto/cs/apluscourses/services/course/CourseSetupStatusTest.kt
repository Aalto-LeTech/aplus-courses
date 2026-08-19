package fi.aalto.cs.apluscourses.services.course

import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test

@TestApplication
class CourseSetupStatusTest {
    private val projectFixture = projectFixture()

    private fun status() = CourseSetupStatus.getInstance(projectFixture.get())

    @BeforeEach
    fun clearStatus(): Unit = status().clear()

    @Test
    fun `steps keep the order they were first reported in`() {
        val setup = status()
        setup.report(CourseSetupStatus.JDK, JDK_TITLE, SetupStepState.DONE)
        setup.report(CourseSetupStatus.CONFIGURATION, CONFIGURATION_TITLE, SetupStepState.DONE)
        setup.report(CourseSetupStatus.moduleStep("O1Library"), "O1Library", SetupStepState.RUNNING)

        setup.report(CourseSetupStatus.JDK, JDK_TITLE, SetupStepState.DONE)

        assertEquals(
            listOf(CourseSetupStatus.JDK, CourseSetupStatus.CONFIGURATION, MODULE_STEP_ID),
            setup.visibleSteps.map { it.id }
        )
    }

    @Test
    fun `a step advances in place rather than being listed twice`() {
        val setup = status()
        val step = CourseSetupStatus.moduleStep("O1Library")
        setup.report(step, "O1Library", SetupStepState.RUNNING)
        setup.update(step, SetupStepState.RUNNING, DOWNLOAD_DETAIL)
        setup.update(step, SetupStepState.DONE)

        val only = setup.visibleSteps.single()
        assertEquals(SetupStepState.DONE, only.state)
        assertEquals("O1Library", only.title, "the title survives an update that does not set one")
    }

    @Test
    fun `it is finished only once every step is done`() {
        val setup = status()
        setup.report(CourseSetupStatus.CONFIGURATION, CONFIGURATION_TITLE, SetupStepState.DONE)
        setup.report(CourseSetupStatus.moduleStep("O1Library"), "O1Library", SetupStepState.RUNNING)

        assertFalse(setup.isFinished, "a running module still has work left")

        setup.update(CourseSetupStatus.moduleStep("O1Library"), SetupStepState.DONE)
        assertTrue(setup.isFinished)
    }

    @Test
    fun `a step that needs the student keeps the checklist visible`() {
        val setup = status()
        setup.report(CourseSetupStatus.CONFIGURATION, CONFIGURATION_TITLE, SetupStepState.DONE)
        setup.report(
            CourseSetupStatus.PLUGINS,
            PLUGIN_TITLE,
            SetupStepState.ACTION_NEEDED,
            ENABLE_DETAIL,
            SetupAction.INSTALL_PLUGINS
        )

        assertFalse(setup.isFinished)
        assertEquals(SetupAction.INSTALL_PLUGINS, setup.visibleSteps.last().action)
    }

    @Test
    fun `every terminal state resolves a running step`() {
        val terminal = listOf(
            SetupStepState.DONE,
            SetupStepState.ACTION_NEEDED,
            SetupStepState.FAILED,
        )
        terminal.forEach { state ->
            val setup = status()
            setup.clear()
            setup.report(CourseSetupStatus.PLUGINS, PLUGINS_TITLE, SetupStepState.RUNNING)
            setup.update(CourseSetupStatus.PLUGINS, state)

            assertNotEquals(
                SetupStepState.RUNNING,
                setup.visibleSteps.single().state,
                "$state should take the step out of RUNNING"
            )
        }
    }

    @Test
    fun `updating a step that was never reported does nothing`() {
        val setup = status()
        setup.update(CourseSetupStatus.moduleStep("Ghost"), SetupStepState.RUNNING)

        assertTrue(setup.visibleSteps.isEmpty(), "an unknown id must not invent a nameless row")
    }

    private companion object {
        @NonNls
        const val JDK_TITLE = "Java runtime"

        @NonNls
        const val CONFIGURATION_TITLE = "Configuration"

        @NonNls
        const val PLUGINS_TITLE = "Required plugins"

        @NonNls
        const val PLUGIN_TITLE = "Scala"

        @NonNls
        const val ENABLE_DETAIL = "Enable it in Settings"

        @NonNls
        const val DOWNLOAD_DETAIL = "14 MB of 22 MB"

        @NonNls
        const val MODULE_STEP_ID = "module:O1Library"
    }
}
