package fi.aalto.cs.apluscourses.services.exercise

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.AppLevel
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView.ExerciseGroupItem
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView.ExerciseItem
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView.ExercisesTreeItem
import java.util.Collections
import kotlin.reflect.KClass

@Service(Service.Level.APP)
@State(
    name = "ExercisesUpdaterService",
    storages = [Storage("aplusCoursesExercisesTreeFilter.xml")]
)
class ExercisesTreeFilter {
    private val filters: MutableMap<Filter<out ExercisesTreeItem>, Boolean> =
        Collections.synchronizedMap(mutableMapOf())

    fun setFilter(filter: Filter<out ExercisesTreeItem>, enabled: Boolean) {
        filters[filter] = enabled
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().messageBus
                .syncPublisher(TOPIC)
                .onFilterUpdated()
        }
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
            .map { it.key.getFilterFunction() as (T) -> Boolean }
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
            private val filter: (ExerciseItem) -> Boolean
        ) : Filter<ExerciseItem>(displayName, ExerciseItem::class) {
            override fun getFilterFunction(): (ExerciseItem) -> Boolean = filter
        }

        class ExerciseGroupFilter(
            displayName: String,
            private val filter: (ExerciseGroupItem) -> Boolean
        ) : Filter<ExerciseGroupItem>(displayName, ExerciseGroupItem::class) {
            override fun getFilterFunction(): (ExerciseGroupItem) -> Boolean = filter
        }

        companion object {
            val NON_SUBMITTABLE = ExerciseItemFilter(
                "presentation.exerciseFilterOptions.nonSubmittable"
            ) { item -> !item.exercise.isSubmittable }

            val COMPLETED = ExerciseItemFilter(
                "presentation.exerciseFilterOptions.Completed"
            ) { item -> item.exercise.isCompleted() }

            val OPTIONAL = ExerciseItemFilter(
                "presentation.exerciseFilterOptions.Optional"
            ) { item -> item.exercise.isOptional }

            val CLOSED = ExerciseGroupFilter(
                "presentation.exerciseGroupFilterOptions.Closed"
            ) { item -> !item.group.isOpen }

            val allFilters = listOf(NON_SUBMITTABLE, COMPLETED, OPTIONAL, CLOSED)
        }
    }

    companion object {
        @AppLevel
        val TOPIC: Topic<ExercisesTreeFilterListener> =
            Topic(ExercisesTreeFilterListener::class.java, Topic.BroadcastDirection.TO_CHILDREN)
    }
}

typealias ExercisesItemFilter = (ExerciseItem) -> Boolean
typealias ExercisesGroupFilter = (ExerciseGroupItem) -> Boolean