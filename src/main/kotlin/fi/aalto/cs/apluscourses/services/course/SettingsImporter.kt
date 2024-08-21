package fi.aalto.cs.apluscourses.services.course

import com.intellij.compiler.CompilerWorkspaceConfiguration
import com.intellij.diagnostic.VMOptions
import com.intellij.ide.startup.StartupActionScriptManager
import com.intellij.ide.startup.StartupActionScriptManager.UnzipCommand
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.updateSettings.impl.UpdateSettings
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.openapi.util.io.FileUtilRt
import fi.aalto.cs.apluscourses.model.Course
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.PluginSettings
import fi.aalto.cs.apluscourses.utils.APlusLogger
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import net.lingala.zip4j.ZipFile
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths

@Service(Service.Level.PROJECT)
class SettingsImporter(
    private val project: Project,
    val cs: CoroutineScope
) {
    /**
     * Downloads the course IDE settings ZIP file to a temporary file. Also adds IDEA startup actions
     * that unzip the temporary file to the IDEA configuration path after which the temporary file is
     * deleted. Therefore, the new IDE settings only take effect once IDEA is restarted and the
     * temporary file must still exist at that point.
     *
     * @throws IOException If an IO error occurs (e.g., network issues).
     */
    @Throws(IOException::class)
    suspend fun importIdeSettings(resourceUrls: Map<String, Url>): Boolean {
        val ideSettingsUrl =
            if (SystemInfoRt.isWindows) {
                resourceUrls["ideSettingsWindows"]
            } else if (SystemInfoRt.isLinux) {
                resourceUrls["ideSettingsLinux"]
            } else if (SystemInfoRt.isMac) {
                resourceUrls["ideSettingsMac"]
            } else {
                null
            }
                ?: resourceUrls["ideSettings"] // Use generic IDE settings if no platform-specific settings are available ?: return

        if (ideSettingsUrl == null) false

        val file = FileUtilRt.createTempFile("course-ide-settings", ".zip", false)
        CoursesClient.getInstance(project).fetch(ideSettingsUrl.toString(), file)
        val configPath = FileUtilRt.toSystemIndependentName(PathManager.getConfigPath())
        StartupActionScriptManager.addActionCommands(
            listOf(
                UnzipCommand(file.toPath(), Path.of(configPath)),
                StartupActionScriptManager.DeleteCommand(file.toPath())
            )
        )

        UpdateSettings.getInstance().forceCheckForUpdateAfterRestart()
        return true
    }

    /**
     * Imports the VM options from the course configuration file into the IDE. If there are no
     * options to import, this function does nothing.
     */
    @Throws(IOException::class)
    fun importVMOptions(options: Map<String, String>) {
        if (!VMOptions.canWriteOptions()) {
            logger.warn("Cannot import VM options because the IDE is configured not to use them")
            return
        }
        options.forEach { (key, value) -> VMOptions.setProperty(key, value) }

        logger.info("Imported " + options.size + " VM options")
    }

    /**
     * Downloads the course project settings ZIP file to a temporary file. After that, the files from
     * the .idea directory of the ZIP file are extracted to the .idea directory of the given project,
     * after which the project is reloaded. If the course does not provide custom project settings,
     * this method does nothing.
     *
     * @throws IOException If an IO error occurs (e.g., network issues).
     */
    @Throws(IOException::class)
    suspend fun importProjectSettings(resourceUrls: Map<String, Url>) {
        val settingsUrl = resourceUrls["projectSettings"] ?: return
        val settingsPath = Path.of(project.basePath!!)
        CoursesClient.getInstance(project).getAndUnzip(settingsUrl.toString(), settingsPath)

        // a hard-coded workspace setting
        CompilerWorkspaceConfiguration.getInstance(project).AUTO_SHOW_ERRORS_IN_EDITOR = false
        logger.info("Imported project settings")
    }

    /**
     * Downloads the feedback CSS and saves it to the MainViewModel.
     *
     * @throws IOException If an IO error occurs (e.g., network issues).
     */
    @Throws(IOException::class)
    suspend fun importFeedbackCss(course: Course): String? {
        val cssUrl = course.resourceUrls["feedbackCss"] ?: return null
        println("importing feedback css\n$cssUrl")
        val res = CoursesClient.getInstance(project).get(cssUrl.toString())
        println(res)
        println(res.request)
        println(res.request.url)
        println(res.request.headers)
        println(res.bodyAsText())
        return CoursesClient.getInstance(project).get(cssUrl.toString()).bodyAsText()

        logger.info("Imported feedback CSS")
    }

    fun importScalaReplAdditionalArguments(string: String) {
        Path.of(project.basePath!!)
            .resolve(Project.DIRECTORY_STORE_FOLDER)
            .resolve(PluginSettings.REPL_ADDITIONAL_ARGUMENTS_FILE_NAME)
            .toFile()
            .writeText(string)
    }

    companion object {
        private val logger = APlusLogger.logger

        fun getInstance(project: Project): SettingsImporter {
            return project.service<SettingsImporter>()
        }

        @Throws(IOException::class)
        private fun extractZipTo(zipFile: ZipFile, target: Path) {
            val fileNames = getZipFileNames(zipFile)
            for (fileName in fileNames) {
                val path = Paths.get(fileName)
                // The ZIP contains a .idea directory with all the settings files.
                // We want to extract the files to the .idea directory without the .idea prefix,
                // as otherwise we would end up with .idea/.idea/<settings_files>.
                val pathWithoutRoot = path.subpath(1, path.nameCount)
                zipFile.extractFile(path.toString(), target.toString(), pathWithoutRoot.toString())
            }
        }


        /**
         * Returns the names of the files inside the given ZIP file. Directories are not included, but
         * files inside directories are included.
         *
         * @param zipFile The ZIP file from which the file names are read.
         * @return A list of names of the files inside the given ZIP file.
         * @throws IOException If an IO error occurs.
         */
        @Throws(IOException::class)
        private fun getZipFileNames(zipFile: ZipFile): List<String> {
            return zipFile
                .fileHeaders
                .filter { !it.isDirectory }
                .map { it.fileName }
        }
    }
}
