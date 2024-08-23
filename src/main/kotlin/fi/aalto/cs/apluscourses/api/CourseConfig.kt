package fi.aalto.cs.apluscourses.api

import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.notifications.CourseConfigurationError
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.Notifier
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.utils.Version
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException
import java.nio.channels.UnresolvedAddressException

object CourseConfig {
    /**
     * This class serves as the serializer and documentation for the course configuration file.
     *
     * Example configuration file:
     * ```
     * {
     *     "id": "197",
     *     "name": "O1",
     *     "aPlusUrl": "https://plus.cs.aalto.fi/",
     *     "languages": [
     *         "fi",
     *         "en"
     *     ],
     *     "resources": {
     *         "ideSettings": "https://grader.cs.aalto.fi/static/O1_2021/projects/o1_2021_ij_settings.zip",
     *         "ideSettingsMac": "https://grader.cs.aalto.fi/static/O1_2021/projects/o1_2021_ij_mac_settings.zip",
     *         "projectSettings": "https://grader.cs.aalto.fi/static/O1_2021/projects/o1_2021_project_settings.zip"
     *     },
     *     "vmOptions": {
     *         "file.encoding": "UTF-8"
     *     },
     *     "autoInstall": [
     *         "O1Library"
     *     ],
     *     "repl": {
     *         "initialCommands": {
     *             "Adventure": [
     *                 "import o1.adventure._"
     *             ],
     *             "AdventureDraft": [
     *                 "import o1.adventure.draft._"
     *             ],
     *             "Aliohjelmia": [
     *                 "import o1._",
     *                 "import o1.aliohjelmia._"
     *             ]
     *         }
     *     },
     *     "modules": [
     *         {
     *             "name": "Adventure",
     *             "url": "https://grader.cs.aalto.fi/static/O1_2021/projects/given/Adventure/Adventure.zip",
     *             "version": "1.0",
     *             "changelog": ""
     *         },
     *         {
     *             "name": "AdventureDraft",
     *             "url": "https://grader.cs.aalto.fi/static/O1_2021/projects/given/AdventureDraft/AdventureDraft.zip",
     *             "version": "1.0",
     *             "changelog": ""
     *         },
     *         {
     *             "name": "Aliohjelmia",
     *             "url": "https://grader.cs.aalto.fi/static/O1_2021/projects/given/Aliohjelmia/Aliohjelmia.zip",
     *             "version": "1.0",
     *             "changelog": ""
     *         }
     *     ],
     *     "exerciseModules": {
     *         "31353": {
     *             "en": "Subprograms",
     *             "fi": "Aliohjelmia"
     *         },
     *         "31383": {
     *             "en": "IntroOOP",
     *             "fi": "Oliointro"
     *         },
     *         "31427": {
     *             "en": "Ave",
     *             "fi": "Ave"
     *         }
     *     }
     * }
     * ```
     *
     * @property id ID for the course from the A+ API.
     * `"id": "197"`
     * @property name Name of the course that gets shown in the UI.
     * `"name": "O1"`
     * @property aPlusUrl URL for A+.
     * `"aPlusUrl": "https://plus.cs.aalto.fi/"`
     * @property languages An array of the languages for the assignments. Different languages may use different modules.
     * `"languages": ["fi", "en"]`
     * @property version Minimum version of the plugin required to use the course in the format major.minor.
     * `"version": "4.0"`
     * @property resources URLs for some resources the plugin uses:
     * * ### ideSettings
     *     A .zip file, containing settings for IntelliJ that the user may optionally install while turning their project into an A+ project.
     * * ### ideSettingsMac
     *     Same as before, but for macOS.
     * * ### projectSettings
     *     A .zip file, containing settings for the project the course is installed in, such as inspection profiles and code styles.
     * * ### customProperties
     *     A .properties file, that overwrites the UI texts for the plugin. The default file may be found [here](https://github.com/Aalto-LeTech/aplus-courses/blob/master/src/main/resources/resources.properties).
     * `
     * "resources": {
     *     "ideSettings": "https://grader.cs.aalto.fi/static/O1_2021/projects/o1_2021_ij_settings.zip",
     *     "ideSettingsMac": "https://grader.cs.aalto.fi/static/O1_2021/projects/o1_2021_ij_mac_settings.zip",
     *     "projectSettings": "https://grader.cs.aalto.fi/static/O1_2021/projects/o1_2021_project_settings.zip"
     * }
     * `
     * @property vmOptions A map of options for the JVM.
     * `"vmOptions": {"file.encoding": "UTF-8"}`
     * @property optionalCategories An array of categories of the assignments that are optional.
     * `"optionalCategories": ["training", "challenge"]`
     * @property autoInstall An array of the modules that get installed automatically when the project gets turned into an A+ project.
     * `"autoInstall": ["O1Library"]`
     * @property repl Settings for the Scala REPL. See [REPL].
     * @property modules An array of objects containing information about modules. See [Module].
     * @property exerciseModules Information, about which module each assignment uses. The assignment ID is used as a key to a map, where language codes are used as keys for the module names.
     * `
     * "exerciseModules": {
     *     "31353": {
     *         "en": "Subprograms",
     *         "fi": "Aliohjelmia"
     *     }
     * }
     * `
     * @property hiddenElements
     **/
    @Serializable
    data class JSON(
        val id: String,
        val name: String,
        val aPlusUrl: String,
        val languages: List<String>,
        val version: Version = Version.DEFAULT,
        val resources: Resources? = null,
        val requiredPlugins: List<RequiredPlugin> = emptyList(),
        val vmOptions: Map<String, String> = emptyMap(),
        val optionalCategories: List<String> = emptyList(),
        val autoInstall: List<String> = emptyList(),
        val scalaRepl: ScalaREPL? = null,
        val modules: List<Module>,
        val callbacks: Callbacks? = null,
        val exerciseModules: Map<Long, Map<String, String>>,
        val hiddenElements: List<Long> = emptyList(),
        val grading: Grading? = null,
        val alwaysShowGroups: Boolean = false,
    )

    @Serializable
    data class RequiredPlugin(
        val name: String,
        val id: String,
    )

    @Serializable
    data class Resources(
        val ideSettings: String? = null,
        val ideSettingsMac: String? = null,
        val projectSettings: String? = null,
        val customProperties: String? = null,
        val feedbackCss: String? = null,
    )

    fun resourceUrls(res: Resources?): Map<String, Url> {
        if (res == null) return emptyMap()
        return mapOf(
            "ideSettings" to res.ideSettings,
            "ideSettingsMac" to res.ideSettingsMac,
            "projectSettings" to res.projectSettings,
            "customProperties" to res.customProperties,
            "feedbackCss" to res.feedbackCss,
        ).mapNotNull { (key, value) ->
            value?.let { key to Url(it) }
        }.toMap()
    }

    @Serializable
    data class Callbacks(
        val postDownloadModule: List<String> = emptyList()
    )

    /**
     * ```
     * "repl": {
     *     "initialCommands": {
     *         "Adventure": [
     *             "import o1.adventure._"
     *         ]
     *     },
     *     "arguments": "-new-syntax -feature -deprecation -explain-types"
     * }
     * ```
     * @property initialCommands A map from a module name to an array of commands that get run when opening the REPL for the given module.
     * @property arguments Arguments for the Scala compiler of the REPL.
     */
    @Serializable
    data class ScalaREPL(
        val initialCommands: Map<String, List<String>> = emptyMap(),
        val arguments: String? = null,
    )

    /**
     * ```
     * "modules": [
     *     {
     *     "name": "Adventure",
     *     "language": "en",
     *     "url": "https://grader.cs.aalto.fi/static/O1_2021/projects/given/Adventure/Adventure.zip",
     *     "version": "2.3",
     *     "changelog": "Fixed game crashing on some inputs."
     *     }
     * ]
     * ```
     * @property name Name for the module shown in the UI.
     * @property url URL to a .zip file containing skeleton code, that the plugin downloads.
     * @property language Optional language of the module. If provided, only shown for that language in the UI.
     * @property version Major.minor version number. Increment the major number when making breaking changes, else the minor number.
     * @property changelog Optional changelog, that gets shown as a tooltip in the modules list. Use empty string or leave out if you don't want a changelog.
     */
    @Serializable
    data class Module(
        val name: String,
        val url: String,
        val language: String? = null,
        val version: Version = Version.DEFAULT,
        val changelog: String? = null,
    )

    @Serializable
    data class Grading(
        val style: String,
        val points: Map<String, Map<String, Int>>
    )

    suspend fun get(project: Project): JSON? {
        val url = CourseFileManager.getInstance(project).state.url ?: return null
        try {
            val courseConfig = CoursesClient.getInstance(project).get(url).bodyAsText()
            return deserialize(courseConfig)
        } catch (e: Exception) {
            when (e) {
                is IOException, is UnresolvedAddressException -> throw e
                else -> {
                    Notifier.notify(CourseConfigurationError(e), project)
                    return null
                }
            }
        }
    }

    fun deserialize(json: String): JSON {
        return jsonSerializer.decodeFromString(JSON.serializer(), json)
    }

    private val jsonSerializer = Json { ignoreUnknownKeys = true }
}