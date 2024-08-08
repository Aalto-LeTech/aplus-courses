package fi.aalto.cs.apluscourses.services

//import com.intellij.ide.util.PropertiesComponent
//import com.intellij.openapi.observable.properties.PropertyGraph
//import com.intellij.openapi.project.Project
//import com.intellij.openapi.project.ProjectManager
//import com.intellij.openapi.project.ProjectManagerListener
//import fi.aalto.cs.apluscourses.intellij.services.DefaultGroupIdSetting
//import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider
//import fi.aalto.cs.apluscourses.intellij.utils.CourseFileManager
//import fi.aalto.cs.apluscourses.intellij.utils.IntelliJFilterOption
//import fi.aalto.cs.apluscourses.intellij.utils.ProjectKey
//import fi.aalto.cs.apluscourses.model.Course
//import fi.aalto.cs.apluscourses.model.CourseProject
//import fi.aalto.cs.apluscourses.model.news.NewsTree
//import fi.aalto.cs.apluscourses.model.people.User
//import fi.aalto.cs.apluscourses.presentation.CourseEndedBannerViewModel
//import fi.aalto.cs.apluscourses.presentation.CourseViewModel
//import fi.aalto.cs.apluscourses.presentation.MainViewModel
//import fi.aalto.cs.apluscourses.presentation.filter.Options
//import fi.aalto.cs.apluscourses.presentation.module.ModuleFilter.DownloadedFilter
//import fi.aalto.cs.apluscourses.presentation.module.ModuleFilter.NotDownloadedFilter
//import fi.aalto.cs.apluscourses.presentation.news.NewsTreeViewModel
//import fi.aalto.cs.apluscourses.utils.PluginResourceBundle
//import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty
//import java.util.*
//import java.util.concurrent.ConcurrentHashMap
//import java.util.concurrent.ConcurrentMap
//import java.util.stream.Collectors

//class PluginSettings internal constructor(private val applicationPropertiesManager: PropertiesManager) :
//    MainViewModelProvider, DefaultGroupIdSetting {
//    enum class LocalIdeSettingsNames(val id: String) {
//        A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG("A+.showReplConfigDialog"),
//        A_PLUS_HIDE_REPL_WARNING_BANNER("A+.hideReplWarningBanner"),
//        A_PLUS_IMPORTED_IDE_SETTINGS("A+.importedIdeSettings"),
//        A_PLUS_DEFAULT_GROUP("A+.defaultGroup"),
//        A_PLUS_SHOW_NON_SUBMITTABLE("A+.showNonSubmittable"),
//        A_PLUS_SHOW_NON_DOWNLOADED("A+.showNotDownloaded"),
//        A_PLUS_SHOW_DOWNLOADED("A+.showDownloaded"),
//        A_PLUS_SHOW_COMPLETED("A+.showCompleted"),
//        A_PLUS_SHOW_OPTIONAL("A+.showOptional"),
//        A_PLUS_SHOW_CLOSED("A+.showClosed"),
//        A_PLUS_IS_ASSISTANT_MODE("A+.assistantMode"),
//        A_PLUS_COLLAPSED_PANELS("A+.collapsed"),
//        A_PLUS_READ_NEWS("A+.readNews")
//    }

//    private val mainViewModels: ConcurrentMap<ProjectKey, MainViewModel> = ConcurrentHashMap()
//
//    private val courseProjects: ConcurrentMap<ProjectKey, CourseProject> = ConcurrentHashMap()
//
//    private val courseFileManagers
//            : ConcurrentMap<ProjectKey, CourseFileManager> = ConcurrentHashMap()


//    private val moduleFilterOptions = Options(
//        IntelliJFilterOption(
//            applicationPropertiesManager,
//            LocalIdeSettingsNames.A_PLUS_SHOW_DOWNLOADED,
//            PluginResourceBundle.getText("presentation.moduleFilterOptions.Downloaded"),
//            null,
//            DownloadedFilter()
//        ),
//        IntelliJFilterOption(
//            applicationPropertiesManager,
//            LocalIdeSettingsNames.A_PLUS_SHOW_NON_DOWNLOADED,
//            PluginResourceBundle.getText("presentation.moduleFilterOptions.notDownloaded"),
//            null,
//            NotDownloadedFilter()
//        )
//
//    )

//    private val projectManagerListener: ProjectManagerListener = object : ProjectManagerListener {
//        override fun projectClosed(project: Project) {
//            val key = ProjectKey(project)
//            courseFileManagers.remove(key)
//            val courseProject = courseProjects.remove(key)
//            courseProject?.dispose()
//            val mainViewModel = mainViewModels.remove(key)
//            mainViewModel?.dispose()
//            ProjectManager.getInstance().removeProjectManagerListener(project, this)
//        }
//    }

///**
// * Returns a MainViewModel for a specific project.
// *
// * @param project A project, or null (equivalent for default project).
// * @return A main view model.
// */
//    override fun getMainViewModel(project: Project?): MainViewModel {
//        // ProjectKey takes care or project being null and avoids creating differing keys for null.
//        val key = ProjectKey(project)
//        return mainViewModels.computeIfAbsent(key) { projectKey: ProjectKey? ->
//            if (project != null) {
//                ProjectManager
//                    .getInstance()
//                    .addProjectManagerListener(project, projectManagerListener)
//            }
//            MainViewModel()
//        }
//    }

/**
 * Registers a course project. This creates a main view model. It also starts the updater of the
 * course project. Calling this method again with the same project has no effect.
 */
//    fun registerCourseProject(courseProject: CourseProject) {
//        val key = ProjectKey(courseProject.project)
//        val mainViewModel = getMainViewModel(courseProject.project)
//        mainViewModel.bannerViewModel.set(
//            CourseEndedBannerViewModel(courseProject)
//        )
//        val passwordStorage =
//            TokenStorage(courseProject.course.apiUrl)
//        val authenticationFactory =
//            APlusTokenAuthentication.getFactoryFor(passwordStorage)
//        courseProjects.computeIfAbsent(key) { projectKey: ProjectKey? ->
//            courseProject.course.register()
////            courseProject.readAuthenticationFromStorage(passwordStorage, authenticationFactory)
//            mainViewModel.updateCourseViewModel(courseProject)
// This is needed here, because by default MainViewModel has an ExercisesTreeViewModel that
// assumes that the project isn't a course project. This means that the user would be
// instructed to turn the project into a course project for an example when the token is
// missing.
//            mainViewModel.toolWindowCardViewModel.isAPlusProject = true
//            mainViewModel.toolWindowCardViewModel.setModuleButtonRequiresLogin(
//                courseProject.course.requireAuthenticationForModules
//            )
//            courseProject.user.addValueObserver(
//                mainViewModel,
//                ObservableProperty.Callback { obj: MainViewModel, user: User? -> obj.userChanged(user) })
//            courseProject.user.addSimpleObserver(courseProject) { courseP: CourseProject -> courseP.courseUpdater.restart() }
//            val exercisesViewModel = ExercisesTreeViewModel(ExercisesTree(), Options())
//            mainViewModel.exercisesViewModel.set(exercisesViewModel)
//            mainViewModel.newsTreeViewModel.set(NewsTreeViewModel(NewsTree(), mainViewModel))
//            courseProject.courseUpdated.addListener(
//                mainViewModel.courseViewModel
//            ) { obj: ObservableProperty<CourseViewModel> -> obj.valueChanged() }
//            courseProject.courseUpdated.addListener(mainViewModel) { viewModel: MainViewModel ->
//                viewModel.updateNewsViewModel(
//                    courseProject
//                )
//            }
//            courseProject.exercisesUpdated.addListener(mainViewModel) { viewModel: MainViewModel ->
//                viewModel.updateExercisesViewModel(
//                    courseProject
//                )
//            }
//            courseProject.courseUpdater.restart()
//            courseProject.exercisesUpdater.restart()
//            courseProject
//        }
//    }

//    fun getCourseProject(project: Project?): CourseProject? {
//        return courseProjects[ProjectKey(project)]
//    }

//    private val allCourseProjects: List<CourseProject>
//        /**
//         * Returns a list of all open course projects. The order is arbitrary.
//         *
//         * @return A list.
//         */
//        get() = ArrayList(courseProjects.values)
//
//    /**
//     * Returns the [CourseFileManager] instance corresponding to the given project. A new
//     * instance is created if no instance exists yet.
//     */
//    fun getCourseFileManager(project: Project): CourseFileManager {
//        return courseFileManagers.computeIfAbsent(
//            ProjectKey(project)
//        ) { key: ProjectKey? ->
//            CourseFileManager(
//                project
//            )
//        }
//    }

//    var isAssistantMode: Boolean
//        get() = applicationPropertiesManager.getValue(LocalIdeSettingsNames.A_PLUS_IS_ASSISTANT_MODE.id).toBoolean()
//        /**
//         * Sets the property for showing assistant tools.
//         */
//        set(assistantMode) {
//            applicationPropertiesManager
//                .setValue(
//                    LocalIdeSettingsNames.A_PLUS_IS_ASSISTANT_MODE.id,
//                    assistantMode.toString()
//                )
//        }

//    /**
//     * Sets a collapsible panel expanded.
//     */
//    fun setExpanded(title: String) {
//        val collapsed = getCollapsed() ?: return
//        applicationPropertiesManager.setValue(
//            LocalIdeSettingsNames.A_PLUS_COLLAPSED_PANELS.id,
//            Arrays.stream(collapsed.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
//                .filter { s: String -> s != title }
//                .collect(Collectors.joining(";")))
//    }


//    fun getCollapsed(): String? {
//        return applicationPropertiesManager.getValue(LocalIdeSettingsNames.A_PLUS_COLLAPSED_PANELS.id)
//    }

//    /**
//     * Sets a collapsible panel collapsed.
//     */
//    fun setCollapsed(title: String) {
//        val collapsed = getCollapsed()
//        applicationPropertiesManager.setValue(
//            LocalIdeSettingsNames.A_PLUS_COLLAPSED_PANELS.id,
//            if (collapsed != null) "$collapsed;$title" else title
//        )
//    }

//    fun setNewsRead(id: Long) {
//        val readNews = getReadNews()
//        applicationPropertiesManager.setValue(
//            LocalIdeSettingsNames.A_PLUS_READ_NEWS.id,
//            if (readNews != null) "$readNews;$id" else id.toString()
//        )
//    }

//    fun getReadNews(): String? {
//        return applicationPropertiesManager.getValue(LocalIdeSettingsNames.A_PLUS_READ_NEWS.id)
//    }

//    /** TODO
//     * Method (getter) to check the property, responsible for showing REPL configuration window.
//     */
//    fun shouldShowReplConfigurationDialog(): Boolean {
//        return applicationPropertiesManager.getValue(LocalIdeSettingsNames.A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.id)
//            .toBoolean()
//    }

//    /**
//     * Method (setter) to set property, responsible for showing REPL configuration window.
//     *
//     * @param showReplConfigDialog a boolean value of the flag.
//     */
//    fun setShowReplConfigurationDialog(showReplConfigDialog: Boolean) {
//        applicationPropertiesManager //  a String explicitly
//            .setValue(
//                LocalIdeSettingsNames.A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.id,
//                showReplConfigDialog.toString()
//            )
//    }

//    /** TODO
//     * Whether the REPL window should inform the user that a module for which the REPL is running has changed.
//     */
//    fun shouldHideReplModuleChangedWarning(): Boolean {
//        return applicationPropertiesManager.getValue(LocalIdeSettingsNames.A_PLUS_HIDE_REPL_WARNING_BANNER.id)
//            .toBoolean()
//    }

//    /**
//     * Sets whether the REPL window should inform the user that a module for which the REPL is running has changed.
//     */
//    fun setHideReplModuleChangedWarning(shouldShowWarning: Boolean) {
//        applicationPropertiesManager
//            .setValue(LocalIdeSettingsNames.A_PLUS_HIDE_REPL_WARNING_BANNER.id, shouldShowWarning.toString())
//    }

//    var importedIdeSettingsId: Long?
//        get() = applicationPropertiesManager.getValue(LocalIdeSettingsNames.A_PLUS_IMPORTED_IDE_SETTINGS.id)?.toLong()
//        set(courseId) {
//            applicationPropertiesManager.setValue(
//                LocalIdeSettingsNames.A_PLUS_IMPORTED_IDE_SETTINGS.id,
//                courseId.toString()
//            )
//        }

//    TODO
//    override fun getDefaultGroupId(): Optional<Long> {
//        val id = applicationPropertiesManager.getValue(LocalIdeSettingsNames.A_PLUS_DEFAULT_GROUP.id)
//        return Optional.ofNullable(id).map { s: String -> s.toLong() }
//    }

//    override fun setDefaultGroupId(groupId: Long) {
//        applicationPropertiesManager.setValue(LocalIdeSettingsNames.A_PLUS_DEFAULT_GROUP.id, groupId.toString())
//    }
//
//    override fun clearDefaultGroupId() {
//        applicationPropertiesManager.unsetValue(LocalIdeSettingsNames.A_PLUS_DEFAULT_GROUP.id)
//    }

//    /**
//     * Sets unset local IDE settings to their default values.
//     */
//    fun initializeLocalIdeSettings() {
//        if (!applicationPropertiesManager.isValueSet(LocalIdeSettingsNames.A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.id)) {
//            setShowReplConfigurationDialog(true)
//        }
//        if (!applicationPropertiesManager.isValueSet(LocalIdeSettingsNames.A_PLUS_IMPORTED_IDE_SETTINGS.id)) {
//            importedIdeSettingsId = null
//        }
////        moduleFilterOptions.forEach(Consumer { obj: Option -> obj.init() })
////        exerciseFilterOptions.forEach(Consumer { obj: Option -> obj.init() })
//    }

//    /** TODO
//     * Resets all local settings to their default values.
//     */
//    fun resetLocalSettings() {
//        unsetLocalIdeSettings()
//        initializeLocalIdeSettings()
//    }

//    /**
//     * Unsets all the local IDE settings from [LocalIdeSettingsNames].
//     */
//    fun unsetLocalIdeSettings() {
//        Arrays.stream(LocalIdeSettingsNames.entries.toTypedArray())
//            .map { obj: LocalIdeSettingsNames -> obj.id }
//            .forEach { key: String -> applicationPropertiesManager.unsetValue(key) }
//    }

//    val technicalCourseDescription: String
//        /**
//         * Returns a string that holds technical info about the course projects that are open in the IDE.
//         *
//         * @return A string.
//         */
//        get() = java.lang.String.join(",", allCourseProjects
//            .stream()
//            .map { courseProject: CourseProject -> courseProject.course }
//            .map { obj: Course -> obj.technicalDescription }
//            .toList())

//    interface PropertiesManager {
//        fun getValue(key: String): String?
//
//        fun unsetValue(key: String)
//
//        fun isValueSet(key: String): Boolean
//
//        fun getBoolean(key: String): Boolean {
//            return getBoolean(key, false)
//        }
//
//        fun getBoolean(key: String, defaultValue: Boolean): Boolean {
//            return if (isValueSet(key)) getValue(key).toBoolean() else defaultValue
//        }
//
//        fun setValue(key: String, value: String?)
//
//        /**
//         * Sets property to the given value, or unsets it if the value equals default value.
//         *
//         * @param key          Name of the property
//         * @param value        Value
//         * @param defaultValue Default value
//         * @param <T>          Type of the value
//        </T> */
//        fun <T> setValue(key: String, value: T?, defaultValue: T?) {
//            if (value == defaultValue) {
//                unsetValue(key)
//            } else {
//                setValue(key, value.toString())
//            }
//        }
//    }

//    private class PropertiesComponentAdapter(private val propertiesComponent: PropertiesComponent) : PropertiesManager {
//        override fun setValue(key: String, value: String?) {
//            propertiesComponent.setValue(key, value)
//        }
//
//        override fun getValue(key: String): String? {
//            return propertiesComponent.getValue(key)
//        }
//
//        override fun unsetValue(key: String) {
//            propertiesComponent.unsetValue(key)
//        }
//
//        override fun isValueSet(key: String): Boolean {
//            return propertiesComponent.isValueSet(key)
//        }
//    }

// Initialization-on-demand holder
//    private object InstanceHolder {
//        @JvmStatic
//        val instance: PluginSettings = PluginSettings(PropertiesComponentAdapter(PropertiesComponent.getInstance()))
////            /**
////             * Methods to get the Singleton instance of [PluginSettings].
////             *
////             * @return an instance of [PluginSettings].
////             */
////            get() = InstanceHolder.INSTANCE
//    }

object PluginSettings {
//        @JvmStatic
//        fun getInstance(): PluginSettings {
//            return InstanceHolder.instance
//        }


    const val MODULE_REPL_INITIAL_COMMANDS_FILE_NAME: String = ".repl-commands"

    const val A_PLUS: String = "A+"

    //  15 minutes in milliseconds
    const val UPDATE_INTERVAL: Long = 15L * 60 * 1000

    //  15 seconds in milliseconds
    const val REASONABLE_DELAY_FOR_MODULE_INSTALLATION: Long = 15L * 1000
}
//}
