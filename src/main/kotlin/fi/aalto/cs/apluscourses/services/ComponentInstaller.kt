package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.utils.APlusLogger
import kotlinx.coroutines.*


@Service(Service.Level.PROJECT)
class ComponentInstaller(
    private val project: Project,
    val cs: CoroutineScope
) {
//    protected fun waitUntilLoadedAsync(component: OldComponent): Job {
//        return cs.launch {
//            component.stateMonitor.waitUntil(OldComponent.LOADED)
//        }
//    }
//
//    protected fun installInternalAsync(component: OldComponent): Deferred<Unit> {
//        val installation: Installation = Installation(component)
//        return cs.async {
//            installation.doIt()
//        }
//    }
//
//    protected fun installInternalAsync(components: List<OldComponent>): Job {
//        return cs.launch {
//            components.forEach {
//                installInternalAsync(it).start()
//            }
//        }
//    }
//
////    /**
////     * Installs multiple components and their dependencies.
////     *
////     * @param components A [List] of [OldComponent]s to be installed.
////     */
////    fun install(components: List<OldComponent>) {
////        return components.map {
////            installInternalAsync(it)
////        }.forEach {
////            it.start()
////        }
////        taskManager.joinAll(components
////            .stream()
////            .map { component: Component? -> this.installInternalAsync(component) }
////            .collect(Collectors.toList()))
////    }
//
//    suspend fun newInstall(component: Component<*>, course: Course) {
//        component.downloadAndInstall()
//        val dependencies = component.dependencyNames
//        if (dependencies.isNullOrEmpty()) {
//            return
//        }
//
//    }
//
//    /**
//     * Installs a component and its dependencies.
//     *
//     * @param component A [OldComponent] to be installed.
//     */
//    fun install(component: OldComponent) {
//        installInternalAsync(component).start()
//    }
//
//    fun installAsync(components: List<OldComponent>, callback: Runnable? = null): Job {
//        return cs.launch {
//            installInternalAsync(components)
//            callback?.run()
//        }
//    }
//
//    fun installAsync(component: OldComponent, callback: Runnable? = null): Job {
//        return cs.launch {
//            installInternalAsync(component).await()
//            callback?.run()
//        }
//    }
//
//    private inner class Installation(private val component: OldComponent) {
//        fun doIt() {
//            component.resolveState()
//            unloadIfError()
//            try {
//                if (component.isUpdatable) {
//                    uninstallForUpdate()
//                }
//                fetch()
//                load()
//                waitForDependencies()
//                val course = project.service<CourseManager>().state.course ?: return //TODO
//                component.validate(course)
//            } catch (e: IOException) {
//                logger.info("A component could not be installed", e)
//                component.stateMonitor.set(OldComponent.ERROR)
//            } catch (e: ComponentLoadException) {
//                logger.info("A component could not be installed", e)
//                component.stateMonitor.set(OldComponent.ERROR)
//            } catch (e: NoSuchComponentException) {
//                logger.info("A component could not be installed", e)
//                component.stateMonitor.set(OldComponent.ERROR)
//            }
//        }
//
//        fun unloadIfError() {
//            if (component.stateMonitor.hasError()) {
//                component.unload()
//                component.setUnresolved()
//                component.resolveState()
//            }
//        }
//
//        @Throws(IOException::class)
//        fun uninstallForUpdate() {
//            val todoShouldOverwrite = true // !dialogs.shouldOverwrite(component)
//            if (component.stateMonitor.setConditionallyTo(OldComponent.UNINSTALLING, OldComponent.LOADED)) {
//                if (component.hasLocalChanges() && todoShouldOverwrite) {
//                    abortAction()
//                    return
//                }
//                component.unload()
//                component.remove()
//                component.stateMonitor.set(OldComponent.UNINSTALLED)
//            }
//        }
//
//        fun abortAction() {
//            if (component.stateMonitor.setConditionallyTo(
//                    OldComponent.ACTION_ABORTED,
//                    OldComponent.FETCHING, OldComponent.LOADING, OldComponent.UNINSTALLING
//                )
//            ) {
//                component.setUnresolved()
//                component.resolveState()
//            }
//        }
//
//        @Throws(IOException::class)
//        fun fetch() {
//            if (component.stateMonitor.setConditionallyTo(
//                    OldComponent.FETCHING,
//                    OldComponent.NOT_INSTALLED, OldComponent.UNINSTALLED
//                )
//            ) {
//                component.fetch()
//                component.stateMonitor.set(OldComponent.FETCHED)
//            } else {
//                component.stateMonitor.waitUntil(OldComponent.FETCHED)
//            }
//        }
//
//        @Throws(ComponentLoadException::class)
//        fun load() {
//            if (component.stateMonitor.setConditionallyTo(OldComponent.LOADING, OldComponent.FETCHED)) {
//                component.load()
//                component.stateMonitor.set(OldComponent.LOADED)
//            } else {
//                component.stateMonitor.waitUntil(OldComponent.LOADED)
//            }
//        }
//
//        @Throws(NoSuchComponentException::class)
//        fun waitForDependencies() {
//            if (component.dependencyStateMonitor.setConditionallyTo(
//                    OldComponent.DEP_WAITING,
//                    OldComponent.DEP_INITIAL, OldComponent.DEP_ERROR
//                )
//            ) {
//                val course = CourseManager.course(project)
//                    ?: return // TODO
////                val dependencies = course.components.values [component.dependencies]
//                val dependencies = component.getDependencies().mapNotNull { course.components[it] }
//                dependencies.forEach(Consumer { obj: OldComponent -> obj.resolveState() })
////                course.components
//                installAsync(dependencies)
//                cs.launch {
//                    dependencies.forEach { waitUntilLoadedAsync(it) }
//                }.invokeOnCompletion {
//                    component.dependencyStateMonitor.set(OldComponent.DEP_LOADED)
//                }
////                taskManager.joinAll(dependencies
////                    .stream()
////                    .map { component: Component -> this@ComponentInstallerImpl.waitUntilLoadedAsync(component) }
////                    .collect(Collectors.toList()))
////                component.dependencyStateMonitor.set(Component.DEP_LOADED)
//            } else {
//                component.dependencyStateMonitor.waitUntil(OldComponent.DEP_LOADED)
//            }
//        }
//    }

    companion object {
        private val logger: Logger = APlusLogger.logger
    }
}
