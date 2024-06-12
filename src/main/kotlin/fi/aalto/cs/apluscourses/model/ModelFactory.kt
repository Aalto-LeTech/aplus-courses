package fi.aalto.cs.apluscourses.model

import fi.aalto.cs.apluscourses.utils.Callbacks
import fi.aalto.cs.apluscourses.utils.CourseHiddenElements
import fi.aalto.cs.apluscourses.utils.PluginDependency
import fi.aalto.cs.apluscourses.utils.Version
import java.net.URL

interface ModelFactory {
    fun createCourse(
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
    ): Course

    fun createModule(
        name: String, url: URL, version: Version, changelog: String
    ): Module

    fun createLibrary(name: String): Library
}
