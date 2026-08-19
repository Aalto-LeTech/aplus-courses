package fi.aalto.cs.apluscourses.generator

import com.intellij.ide.projectWizard.ProjectWizardJdkIntent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ui.configuration.projectRoot.SdkDownloadTask
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@NonNls
private const val FAKE_JDK_PREFIX = "fake-jdk-"

@NonNls
private const val JAVA_VERSION_KEY = "JAVA_VERSION"

private class FakeJdkDownloadTask(
    private val home: Path,
    private val version: String
) : SdkDownloadTask {
    @NonNls
    override fun getSuggestedSdkName(): String = "$FAKE_JDK_PREFIX$version"
    override fun getPlannedHomeDir(): String = home.toString()
    override fun getPlannedVersion(): String = version

    override fun doDownload(indicator: ProgressIndicator) {
        home.resolve("bin").createDirectories()
        @NonNls val releaseFile = "$JAVA_VERSION_KEY=\"$version\"\n"
        home.resolve("release").writeText(releaseFile)
    }
}

@TestApplication
class JdkIntentTest {
    private val projectFixture = projectFixture()

    private var registeredJdk: Sdk? = null

    @AfterEach
    fun removeRegisteredJdk() {
        registeredJdk?.let { sdk ->
            runWriteAction { ProjectJdkTable.getInstance().removeJdk(sdk) }
        }
        registeredJdk = null
    }

    @Test
    fun `a JDK chosen for download is materialized into an Sdk and applied to the project`(
        @TempDir tmp: Path
    ): Unit = runBlocking(Dispatchers.EDT) {
        val home = tmp.resolve("jdk-25")
        val task = FakeJdkDownloadTask(home, "25")
        val intent = ProjectWizardJdkIntent.DownloadJdk(task)

        val sdk = requireNotNull(intent.prepareJdk()) { MISSING_SDK_MESSAGE }
        registeredJdk = sdk

        assertEquals("fake-jdk-25", sdk.name)
        assertEquals(home.toString(), sdk.homePath)

        val project = projectFixture.get()
        val builder = APlusModuleBuilder().apply {
            name = "TestCourse"
            contentEntryPath = project.basePath
            moduleFilePath = Path.of(requireNotNull(project.basePath), "TestCourse.iml").toString()
            config.language = LANGUAGE
            config.courseConfigUrl = "https://example.org/course.json"
            config.programmingLanguage = PROGRAMMING_LANGUAGE
            config.jdk = sdk
        }

        builder.commit(project, null, null)

        assertEquals(sdk, ProjectRootManager.getInstance(project).projectSdk)
    }

    @Test
    fun `a downloaded JDK left without roots is configured`(): Unit = runBlocking(Dispatchers.EDT) {
        val home = Path.of(System.getProperty("java.home"))
        val task = object : SdkDownloadTask {
            @NonNls
            override fun getSuggestedSdkName(): String = "$FAKE_JDK_PREFIX-downloaded"
            override fun getPlannedHomeDir(): String = home.toString()
            override fun getPlannedVersion(): String = PLANNED_VERSION
            override fun doDownload(indicator: ProgressIndicator) = Unit // already on disk
        }

        val sdk = requireNotNull(ProjectWizardJdkIntent.DownloadJdk(task).prepareJdk()) { MISSING_SDK_MESSAGE }
        registeredJdk = sdk

        assertEquals(0, sdk.rootProvider.getFiles(OrderRootType.CLASSES).size, "the tracker's callback was lost")

        APlusModuleBuilder.ensureSdkConfigured(sdk)

        assertTrue(
            sdk.rootProvider.getFiles(OrderRootType.CLASSES).isNotEmpty(),
            "the JDK's classes should have been set up"
        )
        assertNotEquals(PLANNED_VERSION, sdk.versionString, "the placeholder version should be replaced")
    }

    private companion object {
        @NonNls
        const val MISSING_SDK_MESSAGE = "a download intent should materialize an Sdk"

        @NonNls
        const val PLANNED_VERSION = "0.planned"

        @NonNls
        const val LANGUAGE = "en"

        @NonNls
        const val PROGRAMMING_LANGUAGE = "python"
    }
}
