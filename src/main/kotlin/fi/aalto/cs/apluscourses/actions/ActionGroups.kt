package fi.aalto.cs.apluscourses.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup

object ActionGroups {
    val NEWS_ACTIONS =
        MyActionGroup("fi.aalto.cs.apluscourses.actions.ActionGroups.NEWS_ACTIONS")
    val MODULE_ACTIONS =
        MyActionGroup("fi.aalto.cs.apluscourses.actions.ActionGroups.MODULE_ACTIONS")
    val EXERCISE_ACTIONS =
        MyActionGroup("fi.aalto.cs.apluscourses.actions.ActionGroups.EXERCISE_ACTIONS")
    val MENU_ACTIONS =
        MyActionGroup("fi.aalto.cs.apluscourses.actions.ActionGroups.MENU_ACTIONS")
    val TOOL_WINDOW_ACTIONS =
        MyActionGroup("fi.aalto.cs.apluscourses.actions.ActionGroups.TOOL_WINDOW_ACTIONS")

    class MyActionGroup(val id: String) {
        fun get() = ActionManager.getInstance().getAction(id) as DefaultActionGroup
    }
}
