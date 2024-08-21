package fi.aalto.cs.apluscourses.actions.exercise

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Toggleable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareToggleAction
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.services.exercise.ExercisesTreeFilter

class FilterActionGroup : DefaultActionGroup(), Toggleable {
    init {
        ExercisesTreeFilter.Filter.allFilters.map { filter ->
            add(FilterAction(filter))
        }
    }

    override fun update(e: AnActionEvent) {
        Toggleable.setSelected(
            e.presentation,
            e.project
                ?.service<ExercisesTreeFilter>()
                ?.isAnyActive() == true
        )
    }

    private class FilterAction(val filter: ExercisesTreeFilter.Filter<*>) :
        DumbAwareToggleAction(message(filter.displayName)) {
        override fun isSelected(e: AnActionEvent): Boolean =
            e.project?.service<ExercisesTreeFilter>()?.getFilter(filter) == false

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            e.project?.service<ExercisesTreeFilter>()?.setFilter(filter, !state)
        }

        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
