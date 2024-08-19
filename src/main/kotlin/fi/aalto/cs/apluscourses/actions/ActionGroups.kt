package fi.aalto.cs.apluscourses.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup

object ActionGroups {
    val EXERCISE_ACTIONS: DefaultActionGroup
        get() = actionGroup("fi.aalto.cs.apluscourses.actions.ActionGroups.EXERCISE_ACTIONS")

    val TOOL_WINDOW_ACTIONS: DefaultActionGroup
        get() = actionGroup("fi.aalto.cs.apluscourses.actions.ActionGroups.TOOL_WINDOW_ACTIONS")

    private fun actionGroup(id: String) = ActionManager.getInstance().getAction(id) as DefaultActionGroup
}
