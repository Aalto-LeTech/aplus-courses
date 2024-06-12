package fi.aalto.cs.apluscourses.model

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfoRt
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.utils.*
import fi.aalto.cs.apluscourses.utils.Version.InvalidVersionStringException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

abstract class Course protected constructor(
    /**
     * Returns the id of the course.
     */
    @JvmField val id: String,
    /**
     * Returns the name of the course.
     *
     * @return The name of the course.
     */
    @JvmField val name: String,
    val htmlUrl: String,
    /**
     * Returns the languages of the course (as ISO 639-1 codes).
     *
     * @return A list of language codes.
     */
    @JvmField val languages: List<String>,
    private val modules: List<Module>,
    private val libraries: List<Library>,
    private val exerciseModules: Map<Long, Map<String, String>>,
    private val resourceUrls: Map<String, URL>,
    val vMOptions: Map<String, String>,
    val optionalCategories: Set<String>,
    private val autoInstallComponentNames: List<String>,
    val replInitialCommands: Map<String, List<String>>,
    val replAdditionalArguments: String,
    val version: Version,
    private val pluginDependencies: List<PluginDependency>,
    val hiddenElements: CourseHiddenElements,
    @JvmField val callbacks: Callbacks,
    private val requireAuthenticationForModules: Boolean,
    val feedbackParser: String?,
    @JvmField val newsParser: String?
) : ComponentSource {
    protected val components: Map<String, Component> =
        Stream.concat(modules.stream(), libraries.stream())
            .collect(
                Collectors.toMap(
                    Function { obj: Component -> obj.getName() }, Function.identity()
                )
            )


    /**
     * Returns the list of all modules in this course. If the course object is created with [ ][Course.fromConfigurationData], then the modules are returned in the order in which they are
     * listed in the course configuration data.
     *
     * @return All modules of this course.
     */
    fun getModules(): List<Module> {
        return Collections.unmodifiableList(modules)
    }

    /**
     * Returns the list of libraries (not including common libraries) of the course.
     *
     * @return Libraries of this course.
     */
    fun getLibraries(): List<Library> {
        return Collections.unmodifiableList(libraries)
    }

    open fun getComponents(): Collection<Component> {
        return components.values
    }

    /**
     * Returns a mapping of exercise IDs to modules. The keys are exercise IDs, and the values are
     * maps from language codes to module names. Note, that some exercises use modules that are not in
     * the course configuration file, so the modules may not be in [Course.getModules].
     */
    fun getExerciseModules(): Map<Long, Map<String, String>> {
        return Collections.unmodifiableMap(
            exerciseModules
        )
    }

    /**
     * Returns a map containing URLs of resources for the course. The keys are the names of the
     * resources and the values are the URLs.
     *
     * @return A map with URLs for various resources related to the course.
     */
    fun getResourceUrls(): Map<String, URL> {
        return Collections.unmodifiableMap(resourceUrls)
    }

    val autoInstallComponents: List<Component?>
        /**
         * Returns a list of components that should be installed automatically for this course.
         *
         * @return A list of components that should be installed automatically for this course.
         */
        get() = autoInstallComponentNames
            .stream()
            .map { name: String -> this.getComponentIfExists(name) }
            .filter { obj: Component? -> Objects.nonNull(obj) }
            .collect(Collectors.toList())

    val appropriateIdeSettingsUrl: URL?
        /**
         * Returns a URL containing the appropriate IDE settings for the platform that the user
         * is currently using. If no IDE settings are available, null is returned.
         */
        get() {
            var ideSettingsUrl: URL? = null

            if (SystemInfoRt.isWindows) {
                ideSettingsUrl = resourceUrls["ideSettingsWindows"]
            } else if (SystemInfoRt.isLinux) {
                ideSettingsUrl = resourceUrls["ideSettingsLinux"]
            } else if (SystemInfoRt.isMac) {
                ideSettingsUrl = resourceUrls["ideSettingsMac"]
            }

            if (ideSettingsUrl == null) {
                ideSettingsUrl = resourceUrls["ideSettings"]
            }

            return ideSettingsUrl
        }

    override fun getComponentIfExists(name: String): Component? {
        return components[name]
    }

    /**
     * Resolves states of unresolved components and calls `validate()`.
     */
    fun resolve() {
        getComponents().forEach(Consumer { obj: Component -> obj.resolveState() })
        validate()
    }

    /**
     * Validates that components conform integrity constraints.
     */
    fun validate() {
        for (component in getComponents()) {
            component.validate(this)
        }
    }

    val updatableModules: List<Module>
        /**
         * Gets modules that could be updated.
         *
         * @return A list of modules.
         */
        get() = getModules()
            .stream()
            .filter { obj: Module -> obj.isUpdatable }
            .collect(Collectors.toList())

    open fun register() {
        // Subclasses may do things.
    }

    open fun unregister() {
        // Subclasses may do things.
    }

    val apiUrl: String
        get() = htmlUrl + "api/v2/"

    val courseApiUrl: String
        get() = apiUrl + "courses/" + id

    fun getAutoInstallComponentNames(): List<String> {
        return Collections.unmodifiableList(autoInstallComponentNames)
    }

    val requiredPlugins: List<PluginDependency>
        get() = Collections.unmodifiableList(
            pluginDependencies
        )

    fun requiresLoginForModules(): Boolean {
        return requireAuthenticationForModules
    }

    abstract val exerciseDataSource: ExerciseDataSource

    val technicalDescription: String
        get() = String.format("%s <%s>", name, courseApiUrl)

    companion object {
        @Throws(ResourceException::class, MalformedCourseConfigurationException::class)
        fun fromResource(resourceName: String, factory: ModelFactory): Course {
            val reader: Reader = InputStreamReader(Resources.DEFAULT.getStream(resourceName))
            return fromConfigurationData(reader, resourceName, factory)
        }

        /**
         * Creates a course instance from the course configuration data in the given reader.
         *
         * @param reader A reader providing a character stream with the course configuration data.
         * @return A course instance containing the information parsed from the configuration data.
         * @throws MalformedCourseConfigurationException If the configuration data is malformed in any
         * way
         */
        @Throws(MalformedCourseConfigurationException::class)
        fun fromConfigurationData(reader: Reader, factory: ModelFactory): Course {
            return fromConfigurationData(reader, "", factory)
        }

        /**
         * Creates a course instance from the course configuration data in the given reader.
         *
         * @param reader     A reader providing a character stream with the course configuration data.
         * @param sourcePath The path to the source of the reader, which is stored in exceptions thrown
         * from this method.
         * @return A course instance containing the information parsed from the configuration data.
         * @throws MalformedCourseConfigurationException If the configuration data is malformed in any
         * way
         */
        @Throws(MalformedCourseConfigurationException::class)
        fun fromConfigurationData(
            reader: Reader,
            sourcePath: String,
            factory: ModelFactory
        ): Course {
            val jsonObject = getCourseJsonObject(reader, sourcePath)
            val courseId = getCourseId(jsonObject, sourcePath)
            val courseName = getCourseName(jsonObject, sourcePath)
            val aplusUrl = getCourseAPlusUrl(jsonObject, sourcePath)
            val languages = getCourseLanguages(jsonObject, sourcePath)
            val courseModules = getCourseModules(jsonObject, sourcePath, factory)
            val exerciseModules = getCourseExerciseModules(jsonObject, sourcePath)
            val resourceUrls = getCourseResourceUrls(jsonObject, sourcePath)
            val vmOptions = getInitVMOptions(jsonObject, sourcePath)
            val optionalCategories = getCourseOptionalCategories(jsonObject, sourcePath)
            val autoInstallComponentNames = getCourseAutoInstallComponentNames(jsonObject, sourcePath)
            val replInitialCommands = getCourseReplInitialCommands(jsonObject, sourcePath)
            val replAdditionalArguments = getCourseReplAdditionalArguments(jsonObject, sourcePath)
            val courseVersion = getCourseVersion(jsonObject, sourcePath)
            val pluginDependencies = getCoursePluginDependencies(jsonObject, sourcePath)
            val hiddenElements = getCourseHiddenElements(jsonObject, sourcePath)
            val callbacks = getCourseCallbacks(jsonObject, sourcePath)
            val requireAuthenticationForModules = jsonObject.optBoolean("requireAuthenticationForModules", false)
            val feedbackParser = jsonObject.optString("feedbackParser", null)
            val newsParser = jsonObject.optString("newsParser", null)
            val courseLastModified = jsonObject.optLong("courseLastModified")
            return factory.createCourse(
                courseId,
                courseName,
                aplusUrl,
                languages,
                courseModules,
                emptyList(),  // libraries
                exerciseModules,
                resourceUrls,
                vmOptions,
                optionalCategories,
                autoInstallComponentNames,
                replInitialCommands,
                replAdditionalArguments,
                courseVersion,
                pluginDependencies,
                hiddenElements,
                callbacks,
                requireAuthenticationForModules,
                feedbackParser,
                newsParser,
                courseLastModified
            )
        }

        /**
         * Creates a course instance from the course configuration file at the given URL.
         *
         * @param url The URL of the course configuration file.
         * @return A course instance containing the information parsed from the course configuration file.
         * @throws IOException                           If an IO error occurs (network connection
         * issues for an example).
         * @throws MalformedCourseConfigurationException If the course configuration file is malformed
         * in any way.
         */
        @Throws(IOException::class, MalformedCourseConfigurationException::class)
        suspend fun fromUrl(url: URL, modelFactory: ModelFactory, project: Project): Course {
            val inputStream: InputStream = project.service<CoursesClient>().fetch(url)
            return fromConfigurationData(
                InputStreamReader(inputStream, StandardCharsets.UTF_8), url.toString(), modelFactory
            )
        }

        @Throws(MalformedCourseConfigurationException::class)
        private fun getCourseJsonObject(reader: Reader, source: String): JSONObject {
            val tokenizer = JSONTokener(reader)
            try {
                return JSONObject(tokenizer)
            } catch (ex: JSONException) {
                throw MalformedCourseConfigurationException(
                    source,
                    "Course configuration file should consist of a valid JSON object", ex
                )
            }
        }

        @Throws(MalformedCourseConfigurationException::class)
        private fun getCourseId(jsonObject: JSONObject, source: String): String {
            try {
                return jsonObject.getString("id")
            } catch (ex: JSONException) {
                throw MalformedCourseConfigurationException(
                    source,
                    "Missing or malformed \"id\" key", ex
                )
            }
        }

        @Throws(MalformedCourseConfigurationException::class)
        private fun getCourseName(jsonObject: JSONObject, source: String): String {
            try {
                return jsonObject.getString("name")
            } catch (ex: JSONException) {
                throw MalformedCourseConfigurationException(
                    source,
                    "Missing or malformed \"name\" key", ex
                )
            }
        }

        @Throws(MalformedCourseConfigurationException::class)
        private fun getCourseAPlusUrl(jsonObject: JSONObject, source: String): String {
            try {
                return jsonObject.getString("aPlusUrl")
            } catch (ex: JSONException) {
                throw MalformedCourseConfigurationException(
                    source,
                    "Missing or malformed \"aPlusUrl\" key", ex
                )
            }
        }

        @Throws(MalformedCourseConfigurationException::class)
        private fun getCourseLanguages(
            jsonObject: JSONObject,
            source: String
        ): List<String> {
            val languagesJson = jsonObject.optJSONArray("languages")
                ?: throw MalformedCourseConfigurationException(
                    source, "Missing or malformed \"languages\" key", null
                )
            val languages: MutableList<String> = ArrayList()
            for (i in 0 until languagesJson.length()) {
                try {
                    languages.add(languagesJson.getString(i))
                } catch (e: JSONException) {
                    throw MalformedCourseConfigurationException(
                        source, "\"languages\" array should contain strings", e
                    )
                }
            }
            return languages
        }

        @Throws(MalformedCourseConfigurationException::class)
        private fun getCourseModules(
            jsonObject: JSONObject,
            source: String,
            factory: ModelFactory
        ): List<Module> {
            val modulesJsonArray: JSONArray
            try {
                modulesJsonArray = jsonObject.getJSONArray("modules")
            } catch (ex: JSONException) {
                throw MalformedCourseConfigurationException(
                    source,
                    "Missing or malformed \"modules\" key", ex
                )
            }

            val modules: MutableList<Module> = ArrayList()
            // Indexing loop used to simplify checking that each entry is a JSON object.
            for (i in 0 until modulesJsonArray.length()) {
                try {
                    val moduleObject = modulesJsonArray.getJSONObject(i)
                    modules.add(Module.fromJsonObject(moduleObject, factory))
                } catch (ex: JSONException) {
                    throw MalformedCourseConfigurationException(
                        source,
                        "\"modules\" value should be an array of objects containing module information", ex
                    )
                } catch (ex: MalformedURLException) {
                    throw MalformedCourseConfigurationException(
                        source,
                        "Malformed URL in module object", ex
                    )
                }
            }
            return modules
        }

        @Throws(MalformedCourseConfigurationException::class)
        private fun getCourseExerciseModules(
            `object`: JSONObject,
            source: String
        ): Map<Long, Map<String, String>> {
            val exerciseModules: MutableMap<Long, Map<String, String>> = HashMap()
            val exerciseModulesJson = `object`.optJSONObject("exerciseModules") ?: return exerciseModules

            try {
                val keys = Iterable { exerciseModulesJson.keys() }
                for (exerciseId in keys) {
                    val modules = exerciseModulesJson.getJSONObject(exerciseId)
                    val languageToModule: MutableMap<String, String> = HashMap()
                    val languages = Iterable { modules.keys() }
                    for (language in languages) {
                        languageToModule[language] = modules.getString(language)
                    }
                    exerciseModules[exerciseId.toLong()] = languageToModule
                }
                return exerciseModules
            } catch (e: JSONException) {
                throw MalformedCourseConfigurationException(
                    source,
                    "Malformed \"exerciseModules\" object", e
                )
            }
        }

        @Throws(MalformedCourseConfigurationException::class)
        private fun getCourseResourceUrls(
            jsonObject: JSONObject,
            source: String
        ): Map<String, URL> {
            val resourceUrls: MutableMap<String, URL> = HashMap()
            val resourceUrlsJsonObject = jsonObject.optJSONObject("resources") ?: return resourceUrls
            val keys = Iterable { resourceUrlsJsonObject.keys() }
            for (resourceName in keys) {
                try {
                    val resourceUrl = URL(resourceUrlsJsonObject.getString(resourceName))
                    resourceUrls[resourceName] = resourceUrl
                } catch (ex: JSONException) {
                    throw MalformedCourseConfigurationException(
                        source,
                        "Expected name-url-pairs in \"resources\" object", ex
                    )
                } catch (ex: MalformedURLException) {
                    throw MalformedCourseConfigurationException(
                        source,
                        "Expected name-url-pairs in \"resources\" object", ex
                    )
                }
            }
            return resourceUrls
        }

        @Throws(MalformedCourseConfigurationException::class)
        private fun getInitVMOptions(jsonObject: JSONObject, source: String): Map<String, String> {
            val vmOptions: MutableMap<String, String> = HashMap()
            val vmOptionsJsonObject = jsonObject.optJSONObject("vmOptions") ?: return vmOptions

            val keys = Iterable { vmOptionsJsonObject.keys() }
            for (optionKey in keys) {
                try {
                    vmOptions[optionKey] = vmOptionsJsonObject.getString(optionKey)
                } catch (ex: JSONException) {
                    throw MalformedCourseConfigurationException(
                        source,
                        "Expected string-typed key-value pairs in \"vmOptions\" object", ex
                    )
                }
            }

            return vmOptions
        }

        @Throws(MalformedCourseConfigurationException::class)
        private fun getCourseOptionalCategories(
            jsonObject: JSONObject,
            source: String
        ): Set<String> {
            val categoriesJson = jsonObject.optJSONArray("optionalCategories")
                ?: return setOf("training", "challenge") // defaults for older O1 courses
            val categories: MutableSet<String> = HashSet()
            for (i in 0 until categoriesJson.length()) {
                try {
                    categories.add(categoriesJson.getString(i))
                } catch (e: JSONException) {
                    throw MalformedCourseConfigurationException(
                        source, "\"optionalCategories\" array should contain strings", e
                    )
                }
            }
            return categories
        }

        @Throws(MalformedCourseConfigurationException::class)
        private fun getCourseAutoInstallComponentNames(
            jsonObject: JSONObject,
            source: String
        ): List<String> {
            val autoInstallComponentNames: MutableList<String> = ArrayList()
            val autoInstallArray = jsonObject.optJSONArray("autoInstall") ?: return autoInstallComponentNames

            for (i in 0 until autoInstallArray.length()) {
                try {
                    val autoInstallComponentName = autoInstallArray.getString(i)
                    autoInstallComponentNames.add(autoInstallComponentName)
                } catch (e: JSONException) {
                    throw MalformedCourseConfigurationException(
                        source, "Names in \"autoInstall\" array should be course components", e
                    )
                }
            }
            return autoInstallComponentNames
        }

        @Throws(MalformedCourseConfigurationException::class)
        private fun getCourseReplInitialCommands(
            jsonObject: JSONObject,
            source: String
        ): Map<String, List<String>> {
            val replInitialCommands: MutableMap<String, List<String>> = HashMap()
            jsonObject.optJSONObject("repl") ?: return replInitialCommands
            try {
                val initialCommandsJsonObject = jsonObject
                    .getJSONObject("repl").getJSONObject("initialCommands")

                val keys = Iterable { initialCommandsJsonObject.keys() }
                for (moduleName in keys) {
                    val replCommands = initialCommandsJsonObject
                        .getJSONArray(moduleName)
                        .toList()
                        .stream()
                        .map<String> { obj: Any? -> String::class.java.cast(obj) }
                        .toList()

                    replInitialCommands[moduleName] = replCommands
                }
            } catch (ex: JSONException) {
                throw MalformedCourseConfigurationException(
                    source,
                    "Expected moduleName-commands-pairs in \"repl\" object", ex
                )
            }

            return replInitialCommands
        }

        @Throws(MalformedCourseConfigurationException::class)
        private fun getCourseReplAdditionalArguments(
            jsonObject: JSONObject,
            source: String
        ): String {
            try {
                return jsonObject.optString("replArguments", "")
            } catch (ex: JSONException) {
                throw MalformedCourseConfigurationException(
                    source,
                    "Malformed or non-string \"replArguments\" key", ex
                )
            }
        }

        @Throws(MalformedCourseConfigurationException::class)
        private fun getCourseVersion(
            jsonObject: JSONObject,
            source: String
        ): Version {
            val versionJson = jsonObject.optString("version", null)
                ?: return BuildInfo.INSTANCE.courseVersion

            try {
                return Version.fromString(versionJson)
            } catch (ex: InvalidVersionStringException) {
                throw MalformedCourseConfigurationException(
                    source,
                    "Incomplete or invalid \"version\" object", ex
                )
            }
        }

        @Throws(MalformedCourseConfigurationException::class)
        private fun getCoursePluginDependencies(
            jsonObject: JSONObject,
            source: String
        ): List<PluginDependency> {
            val pluginsJson = jsonObject.optJSONArray("requiredPlugins")
                ?: return emptyList()

            val result: MutableList<PluginDependency> = ArrayList()

            try {
                for (i in 0 until pluginsJson.length()) {
                    val currentPluginJson = pluginsJson.getJSONObject(i)
                    result.add(PluginDependency(currentPluginJson.getString("name"), currentPluginJson.getString("id")))
                }
            } catch (ex: JSONException) {
                throw MalformedCourseConfigurationException(source, "Malformed \"requiredPlugins\" array", ex)
            }

            return result
        }

        @Throws(MalformedCourseConfigurationException::class)
        private fun getCourseHiddenElements(
            jsonObject: JSONObject,
            source: String
        ): CourseHiddenElements {
            jsonObject.optJSONArray("hiddenElements") ?: return CourseHiddenElements()

            try {
                return CourseHiddenElements() //CourseHiddenElements.fromJsonObject(hiddenElements); //TODO
            } catch (ex: JSONException) {
                throw MalformedCourseConfigurationException(source, "Malformed \"hiddenElements\" array", ex)
            }
        }

        @Throws(MalformedCourseConfigurationException::class)
        private fun getCourseCallbacks(
            jsonObject: JSONObject,
            source: String
        ): Callbacks {
            val callbacksJsonObj = jsonObject.optJSONObject("callbacks") ?: return Callbacks()

            try {
                return Callbacks.fromJsonObject(callbacksJsonObj)
            } catch (ex: JSONException) {
                throw MalformedCourseConfigurationException(source, "Malformed \"callbacks\" object", ex)
            }
        }
    }
}
