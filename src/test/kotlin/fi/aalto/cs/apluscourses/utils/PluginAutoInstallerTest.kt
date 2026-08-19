package fi.aalto.cs.apluscourses.utils

import com.intellij.openapi.extensions.PluginId
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import fi.aalto.cs.apluscourses.api.CourseConfig.RequiredPlugin
import fi.aalto.cs.apluscourses.notifications.DisabledPluginsNotification
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test

@TestApplication
class PluginAutoInstallerTest {
    private val projectFixture = projectFixture()

    /** Bundled and depended upon by this plugin, so it is present while tests run. */
    private val bundledJava = PluginId.getId("com.intellij.java")
    private val notAPlugin = PluginId.getId(UNKNOWN_PLUGIN_ID)

    @Test
    fun `a plugin that is not installed has to be downloaded`() {
        assertTrue(PluginAutoInstaller.shouldDownloadPlugin(notAPlugin))
        // It cannot be enabled, because there is nothing to enable yet.
        assertFalse(PluginAutoInstaller.shouldEnablePlugin(notAPlugin))
    }

    @Test
    fun `an installed and enabled plugin needs neither downloading nor enabling`() {
        assertFalse(PluginAutoInstaller.shouldDownloadPlugin(bundledJava))
        assertFalse(PluginAutoInstaller.shouldEnablePlugin(bundledJava))
    }

    @Test
    fun `the disabled-plugins notification names the plugins and offers a way to enable them`() {
        val notification = DisabledPluginsNotification(
            listOf(SCALA_PLUGIN_NAME),
            projectFixture.get()
        )

        assertTrue(
            notification.content.contains(SCALA_PLUGIN_NAME),
            "expected the plugin name in the notification, got: ${notification.content}"
        )
        assertEquals(1, notification.actions.size, "expected an action opening the plugin manager")
    }

    @Test
    fun `ensureDependenciesInstalled reports nothing to do when every plugin is present`(): Unit = runBlocking {
        val project = projectFixture.get()

        val allSatisfied = PluginAutoInstaller.ensureDependenciesInstalled(
            project,
            listOf(RequiredPlugin(id = bundledJava.idString, name = JAVA_PLUGIN_NAME)),
        )

        assertTrue(allSatisfied, "Expected no action to be required for an already-installed plugin")
    }

    private companion object {
        @NonNls
        const val UNKNOWN_PLUGIN_ID = "fi.aalto.cs.definitely-not-a-real-plugin"

        @NonNls
        const val SCALA_PLUGIN_NAME = "Scala"

        @NonNls
        const val JAVA_PLUGIN_NAME = "Java"
    }
}
