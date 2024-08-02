package fi.aalto.cs.apluscourses.model.component.old

import fi.aalto.cs.apluscourses.model.Course
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths

abstract class OldComponent protected constructor(open val originalName: String) {
//    val stateChanged: Event = Event()
//    val onError: Event = Event()

//    @JvmField
//    val stateMonitor: StateMonitor = StateMonitor(UNRESOLVED) { newState: Int -> this.onStateChanged(newState) }
//    val dependencyStateMonitor: StateMonitor =
//        StateMonitor(DEP_INITIAL) { newState: Int -> this.onStateChanged(newState) }

    private var dependencies: List<String>? = null

    abstract val path: Path

    @Throws(IOException::class)
    abstract fun fetch()

    //    @Throws(ComponentLoadException::class)
    abstract fun load()

    open fun unload() {
        dependencies = null
    }

    @Throws(IOException::class)
    open fun remove() {
        // subclasses may do their removal operations
    }

    protected fun onStateChanged(newState: Int) {
//        stateChanged.trigger()
//        if (StateMonitor.isError(newState)) {
//            onError.trigger()
//        }
    }

    /**
     * Tells whether the component is in an error state.
     *
     * @return True if error, otherwise false.
     */
    fun hasError(): Boolean {
        return false
//        val state = stateMonitor.get()
//        return StateMonitor.isError(state) || state == LOADED && dependencyStateMonitor.hasError()
    }

    /**
     * Returns the names of the dependencies.  This method should not be called unless the component
     * is in LOADED state.
     *
     * @return Names of the dependencies, as a [List].
     */
    fun getDependencies(): List<String> = dependencies ?: computeDependencies()

    open val errorCause: Int = ERR_UNKNOWN

    abstract val fullPath: Path

    val documentationIndexFullPath: Path
        get() = Paths.get(fullPath.toString(), "doc/index.html")

    fun documentationExists(): Boolean {
        return true
//        return stateMonitor.get() == LOADED && documentationIndexFullPath.toFile().exists()
    }

    protected abstract fun resolveStateInternal(): Int

    /**
     * If the state is UNRESOLVED, sets it to a state resolved by subclasses.
     */
    fun resolveState() {
//        if (originalName.contains("cala") || originalName.contains("ibrary")) {
//            println("Component:resolveState $originalName $stateMonitor")
//        }
//        if (stateMonitor.get() == UNRESOLVED) {
//            stateMonitor.setConditionallyTo(resolveStateInternal(), UNRESOLVED)
//        }
    }

    protected abstract fun computeDependencies(): List<String>

    /**
     * Checks whether this component's dependencies are in LOADED state.
     *
     * @param componentSource A component source which should have the dependencies of this component.
     * @return True if the dependencies are LOADED, otherwise false.
     */
    private fun areDependenciesLoaded(componentSource: Course): Boolean {
        return true
//        val dependencies = getDependencies()
//        println("Component:areDependenciesLoaded $originalName $dependencies ${
//            dependencies
//                .map { name: String -> componentSource.getComponentIfExists(name) }
//                .map { "it: $it, state: ${it?.stateMonitor?.get()} componentState: ${it?.dependencyStateMonitor?.get()}" }
//        } ${
//            dependencies
//                .map { name: String -> componentSource.getComponentIfExists(name) }
//                .all { component: OldComponent? -> component != null && component.stateMonitor.get() == LOADED }
//        }"
//        )
//        return dependencies.isEmpty() || dependencies
//            .map { name: String -> componentSource.getComponentIfExists(name) }
//            .all { component: OldComponent? -> component != null && component.stateMonitor.get() == LOADED }
    }

    /**
     * Sets component to the unresolved state, unless it is active.
     */
    fun setUnresolved() {
//        stateMonitor.setConditionallyTo(
//            UNRESOLVED,
//            NOT_INSTALLED, FETCHED, LOADED, ERROR, UNINSTALLED, ACTION_ABORTED
//        )
    }

    /**
     * Sets the component in DEP_ERROR state if it does not conform dependency integrity constraints.
     * Sets the component that is in DEP_ERROR state to DEP_LOADED state if dependency integrity
     * constraints are conformed.
     */
    fun validate(componentSource: Course) {
//        var depState: Int = DEP_INITIAL
//        if (stateMonitor.get() == LOADED
//            && (dependencyStateMonitor.get().also { depState = it }) != DEP_WAITING
//        ) {
//            dependencyStateMonitor.setConditionallyTo(
//                if (areDependenciesLoaded(componentSource)) DEP_LOADED else DEP_ERROR, depState
//            )
//        }
    }

    abstract val isUpdatable: Boolean

    abstract fun hasLocalChanges(): Boolean

    fun interface InitializationCallback {
        fun initialize(component: OldComponent)
    }

    companion object {
        const val NOT_INSTALLED: Int = 0
        const val FETCHING: Int = NOT_INSTALLED + 1
        const val FETCHED: Int = FETCHING + 1
        const val LOADING: Int = FETCHED + 1
        const val LOADED: Int = LOADING + 1
        const val UNINSTALLING: Int = LOADED + 1
        const val ERROR: Int = -1
        const val UNRESOLVED: Int = ERROR - 1
        const val UNINSTALLED: Int = UNRESOLVED - 1
        const val ACTION_ABORTED: Int = 99

        const val DEP_INITIAL: Int = 0
        const val DEP_WAITING: Int = DEP_INITIAL + 1
        const val DEP_LOADED: Int = DEP_WAITING + 1
        const val DEP_ERROR: Int = -1

        const val ERR_FILES_MISSING: Int = 0
        const val ERR_UNKNOWN: Int = ERR_FILES_MISSING + 1
    }
}
