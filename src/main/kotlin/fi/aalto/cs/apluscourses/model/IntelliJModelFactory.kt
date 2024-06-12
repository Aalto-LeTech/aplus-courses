package fi.aalto.cs.apluscourses.model

import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.intellij.model.APlusProject
import fi.aalto.cs.apluscourses.intellij.model.CommonLibraryProvider
import fi.aalto.cs.apluscourses.intellij.model.IntelliJCourse
import fi.aalto.cs.apluscourses.model.Component.InitializationCallback
import fi.aalto.cs.apluscourses.services.PluginSettings.Companion.getInstance
import fi.aalto.cs.apluscourses.utils.Callbacks
import fi.aalto.cs.apluscourses.utils.CourseHiddenElements
import fi.aalto.cs.apluscourses.utils.PluginDependency
import fi.aalto.cs.apluscourses.utils.Version
import java.net.URL
import java.time.ZonedDateTime
import java.util.*
import kotlin.random.Random

class IntelliJModelFactory(project: Project) : ModelFactory {
    private val project = APlusProject(project)

    private val modulesMetadata: Map<String, ModuleMetadata> = getInstance()
        .getCourseFileManager(project)
        .modulesMetadata

    override fun createCourse(
        id: String,
        name: String,
        aplusUrl: String,
        languages: List<String>,
        modules: List<Module>,
        libraries: List<Library>,
        exerciseModules: Map<Long, Map<String, String>>,
        resourceUrls: Map<String, URL>,
        vmOptions: Map<String, String>,
        optionalCategories: Set<String>,
        autoInstallComponentNames: List<String>,
        replInitialCommands: Map<String, List<String>>,
        replAdditionalArguments: String,
        courseVersion: Version,
        pluginDependencies: List<PluginDependency>,
        hiddenElements: CourseHiddenElements,
        callbacks: Callbacks,
        requireAuthenticationForModules: Boolean,
        feedbackParser: String?,
        newsParser: String?,
        courseLastModified: Long
    ): Course {
        val course =
            IntelliJCourse(
                id, name, aplusUrl, languages, modules, libraries, exerciseModules,
                resourceUrls, vmOptions, optionalCategories, autoInstallComponentNames, replInitialCommands,
                replAdditionalArguments, courseVersion, project, CommonLibraryProvider(project),
                pluginDependencies, hiddenElements, callbacks, requireAuthenticationForModules,
                feedbackParser, newsParser, courseLastModified
            )

        val componentInitializationCallback =
            InitializationCallback { component: Component -> registerComponentToCourse(component, course) }
        course.commonLibraryProvider.setInitializationCallback(componentInitializationCallback)
        course.getComponents()
            .forEach { component: Component? -> componentInitializationCallback.initialize(component) }

        course.resolve()

        return course
    }

    private fun registerComponentToCourse(component: Component, course: Course) {
        component.onError.addListener(course) { obj: Course -> obj.resolve() }
    }

    override fun createModule(
        name: String,
        url: URL,
        version: Version,
        changelog: String
    ): Module {
        // TODO fix
//        val exampleChangelog = """<ul>
//<li>Added some stuff</li>
//<li>Did some fixing</li>
//</ul>"""
        val exampleChangelog = """Added some stuff<br>
Did some fixing"""

        val exampleVersion = if (Random.nextBoolean()) {
            Version(Random.nextInt(2, 5), Random.nextInt(20))
        } else {
            version
        }
//        val moduleMetadata = Optional.ofNullable(modulesMetadata[name])
//            .orElse(ModuleMetadata(null, null))
        val moduleMetadata = Optional.ofNullable(modulesMetadata[name])
            .orElse(ModuleMetadata(version, ZonedDateTime.now()))
        return IntelliJModule(
            name, url, exampleChangelog, exampleVersion, moduleMetadata.version,
            moduleMetadata.downloadedAt, project
        )
    }

    override fun createLibrary(name: String): Library {
        throw UnsupportedOperationException(
            "Only common libraries like Scala SDK are currently supported."
        )
    }
}
