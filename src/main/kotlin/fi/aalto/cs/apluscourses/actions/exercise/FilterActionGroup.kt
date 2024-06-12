package fi.aalto.cs.apluscourses.actions.exercise

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Toggleable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareToggleAction
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.services.exercise.ExercisesTreeFilterService

@OptIn(ExperimentalStdlibApi::class) // TODO
class FilterActionGroup : DefaultActionGroup(), Toggleable {
    init {
        ExercisesTreeFilterService.Companion.Filter.entries.map { filter ->
            add(FilterAction(filter))
        }
    }

    override fun update(e: AnActionEvent) {
        Toggleable.setSelected(
            e.presentation,
            ApplicationManager.getApplication()
                .service<ExercisesTreeFilterService>()
                .state
                .isAnyActive()
        )
    }

    private class FilterAction(val filter: ExercisesTreeFilterService.Companion.Filter) :
        DumbAwareToggleAction(message(filter.displayName)) {
        override fun isSelected(e: AnActionEvent): Boolean =
            ApplicationManager.getApplication()
                .service<ExercisesTreeFilterService>()
                .state.getFilter(filter)


        override fun setSelected(e: AnActionEvent, state: Boolean) =
            ApplicationManager.getApplication()
                .service<ExercisesTreeFilterService>()
                .state.setFilter(filter, state)

        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
