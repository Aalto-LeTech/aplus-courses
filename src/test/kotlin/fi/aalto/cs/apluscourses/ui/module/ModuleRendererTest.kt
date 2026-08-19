package fi.aalto.cs.apluscourses.ui.module

import com.intellij.openapi.application.EDT
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import fi.aalto.cs.apluscourses.model.component.Component
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.utils.Version
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class ModuleRendererTest {
    private val projectFixture = projectFixture()

    private fun module() = Module(
        name = "O1Library",
        zipUrl = "https://example.org/O1Library.zip",
        changelog = null,
        latestVersion = Version("1.0"),
        language = null,
        project = projectFixture.get()
    )

    @Test
    fun `a cancelled install releases the installing state`(): Unit = runBlocking(Dispatchers.EDT) {
        val module = module()
        val row = ModuleRenderer(module, index = 0, project = projectFixture.get(), onToggle = {})

        module.setLoading()
        row.refreshIcon()
        assertTrue(row.isShowingInstalling, "a module marked LOADING should show as installing")
        assertTrue(row.boundInstallingValue, "the buttons should follow it")

        module.clearLoading()
        row.refreshIcon()

        assertFalse(
            row.isShowingInstalling,
            "the row should stop showing an install once the module settles"
        )
        assertFalse(
            row.boundInstallingValue,
            "the install buttons should come back once the module settles"
        )
    }

    @Test
    fun `a module that is still loading keeps showing the install state`(): Unit =
        runBlocking(Dispatchers.EDT) {
            val module = module()
            val row = ModuleRenderer(module, index = 0, project = projectFixture.get(), onToggle = {})

            module.setLoading()
            row.refreshIcon()

            assertTrue(row.isShowingInstalling)
            assertTrue(row.boundInstallingValue)
            assertTrue(module.status == Component.Status.LOADING)
        }
}
