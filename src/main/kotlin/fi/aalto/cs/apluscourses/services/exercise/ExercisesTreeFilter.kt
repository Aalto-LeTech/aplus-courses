package fi.aalto.cs.apluscourses.services.exercise

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.application
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import com.intellij.util.xmlb.annotations.XCollection
import fi.aalto.cs.apluscourses.BUNDLE
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView.*
import org.jetbrains.annotations.PropertyKey
import java.util.*
import kotlin.reflect.KClass

@Service(Service.Level.PROJECT)
@State(
    name = "Filters",
    storages = [Storage("aplus_project.xml")]
)
class ExercisesTreeFilter(private val project: Project) :
    SimplePersistentStateComponent<ExercisesTreeFilter.State>(State()) {
    class State : BaseState() {
        @get:XCollection(
            propertyElementName = "enabledFilters",
            valueAttributeName = "",
            style = XCollection.Style.v2
        )
        var enabledFilters: MutableList<String> by list()
    }

    private val filters: MutableMap<Filter<out ExercisesTreeItem>, Boolean> =
        Collections.synchronizedMap(mutableMapOf())

    fun setFilter(filter: Filter<out ExercisesTreeItem>, enabled: Boolean) {
        filters[filter] = enabled
        saveState()
        application.invokeLater {
            project.messageBus
                .syncPublisher(TOPIC)
                .onFilterUpdated()
        }
    }

    fun loadFromState() {
        filters.clear()
        state.enabledFilters.forEach { filterName ->
            val filter = Filter.allFilters.find { it.displayName == filterName }
            if (filter != null) {
                filters[filter] = true
            }
        }
    }

    private fun saveState() {
        state.enabledFilters = filters.entries
            .filter { it.value }
            .map { it.key.displayName }.toMutableList()
    }

    fun getFilter(filter: Filter<out ExercisesTreeItem>): Boolean = filters.getOrPut(filter) {
        false
    }

    fun isAnyActive(): Boolean = filters.values.any { it }

    fun exercisesFilter(): ExercisesItemFilter = createFilter(ExerciseItem::class)

    fun exercisesGroupFilter(): ExercisesGroupFilter = createFilter(ExerciseGroupItem::class)

    private fun <T : ExercisesTreeItem> createFilter(targetType: KClass<T>): (T) -> Boolean =
        filters.entries
            // Filter out disabled filters and match the target type
            .filter { it.value && it.key.targetType == targetType }
            // Map to filter functions with correct type
            .map {
                @Suppress("UNCHECKED_CAST", "HardCodedStringLiteral")
                it.key.getFilterFunction() as (T) -> Boolean
            }
            // Combine filters to a single lambda
            .fold({ false }) { acc, filter -> { item -> filter(item) || acc(item) } }

    interface ExercisesTreeFilterListener {
        @RequiresEdt
        fun onFilterUpdated()
    }

    sealed class Filter<T : ExercisesTreeItem>(val displayName: String, val targetType: KClass<T>) {
        abstract fun getFilterFunction(): (T) -> Boolean

        class ExerciseItemFilter(
            displayName: String,
            val filter: (ExerciseItem) -> Boolean
        ) : Filter<ExerciseItem>(displayName, ExerciseItem::class) {
            override fun getFilterFunction(): (ExerciseItem) -> Boolean = filter
        }

        class ExerciseGroupFilter(
            @PropertyKey(resourceBundle = BUNDLE) displayName: String,
            private val filter: (ExerciseGroupItem) -> Boolean
        ) : Filter<ExerciseGroupItem>(displayName, ExerciseGroupItem::class) {
            override fun getFilterFunction(): (ExerciseGroupItem) -> Boolean = filter
        }

        companion object {
            private val NON_SUBMITTABLE: ExerciseItemFilter = ExerciseItemFilter(
                "services.ExercisesTreeFilter.nonSubmittable"
            ) { !it.exercise.isSubmittable }

            private val COMPLETED: ExerciseItemFilter = ExerciseItemFilter(
                "services.ExercisesTreeFilter.Completed"
            ) { it.exercise.isCompleted() }

            private val OPTIONAL: ExerciseItemFilter = ExerciseItemFilter(
                "services.ExercisesTreeFilter.Optional"
            ) { it.exercise.isOptional }

            private val CLOSED: ExerciseGroupFilter = ExerciseGroupFilter(
                "services.ExercisesTreeFilter.Closed"
            ) { !it.group.isOpen }

            val allFilters: List<Filter<out ExercisesTreeItem>> =
                listOf(NON_SUBMITTABLE, COMPLETED, OPTIONAL, CLOSED)
        }
    }

    companion object {
        @ProjectLevel
        val TOPIC: Topic<ExercisesTreeFilterListener> =
            Topic(ExercisesTreeFilterListener::class.java, Topic.BroadcastDirection.TO_CHILDREN)
    }
}

typealias ExercisesItemFilter = (ExerciseItem) -> Boolean
typealias ExercisesGroupFilter = (ExerciseGroupItem) -> Boolean