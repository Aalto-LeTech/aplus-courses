package fi.aalto.cs.apluscourses.services.exercise

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.AppLevel
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView

@Service(Service.Level.APP)
@State(
    name = "ExercisesUpdaterService",
    storages = [Storage("aplusCoursesExercisesTreeFilter.xml")]
)
class ExercisesTreeFilter : SimplePersistentStateComponent<ExercisesTreeFilter.State>(State()) {
    class State : BaseState() {
        private var enabledFilters: MutableMap<String, Boolean> by map()

        fun setFilter(filter: Filter, enabled: Boolean) {
            enabledFilters[filter.name] = enabled
            incrementModificationCount()
            ApplicationManager.getApplication().invokeLater {
                ApplicationManager.getApplication().messageBus
                    .syncPublisher(TOPIC)
                    .onFilterUpdated()
            }
        }

        fun getFilter(filter: Filter): Boolean = enabledFilters.getOrPut(filter.name) {
            incrementModificationCount()
            false
        }

        fun isAnyActive(): Boolean = enabledFilters.values.any { it }

        fun exercisesFilter(): ExercisesItemFilter =
            enabledFilters.entries
                .filter { it.value }
                .mapNotNull { exerciseFilters[it.key] }
                .fold({ false }) { acc, filter -> { item -> acc(item) || filter(item) } }

        fun exercisesGroupFilter(): ExercisesGroupFilter =
            enabledFilters.entries
                .filter { it.value }
                .mapNotNull { exerciseGroupFilters[it.key] }
                .fold({ false }) { acc, filter -> { item -> acc(item) || filter(item) } }

    }

    interface ExercisesTreeFilterListener {
        @RequiresEdt
        fun onFilterUpdated()
    }

    enum class Filter(val displayName: String) {
        NON_SUBMITTABLE("presentation.exerciseFilterOptions.nonSubmittable"),
        COMPLETED("presentation.exerciseFilterOptions.Completed"),
        OPTIONAL("presentation.exerciseFilterOptions.Optional"),
        CLOSED("presentation.exerciseGroupFilterOptions.Closed");
    }

    companion object {
        @AppLevel
        val TOPIC: Topic<ExercisesTreeFilterListener> =
            Topic(ExercisesTreeFilterListener::class.java, Topic.BroadcastDirection.TO_CHILDREN)

        private val nonSubmittableFilter: ExercisesItemFilter = { item: ExercisesView.ExerciseItem ->
            !item.exercise.isSubmittable
        }

        private val completedFilter: ExercisesItemFilter = { item: ExercisesView.ExerciseItem ->
            item.exercise.isCompleted()
        }

        private val optionalFilter: ExercisesItemFilter = { item: ExercisesView.ExerciseItem ->
            item.exercise.isOptional
        }

        private val closedFilter: ExercisesGroupFilter = { item: ExercisesView.ExerciseGroupItem ->
            !item.group.isOpen
        }

        val exerciseFilters = mapOf(
            Filter.NON_SUBMITTABLE.name to nonSubmittableFilter,
            Filter.COMPLETED.name to completedFilter,
            Filter.OPTIONAL.name to optionalFilter
        )

        val exerciseGroupFilters = mapOf(
            Filter.CLOSED.name to closedFilter
        )


    }
}

typealias ExercisesItemFilter = (ExercisesView.ExerciseItem) -> Boolean
typealias ExercisesGroupFilter = (ExercisesView.ExerciseGroupItem) -> Boolean