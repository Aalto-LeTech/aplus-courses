package fi.aalto.cs.apluscourses.ui.temp.courseproject

import com.intellij.openapi.ui.Messages
import fi.aalto.cs.apluscourses.MyBundle.message

class CourseProjectActionDialogs {
    fun showRestartDialog(): Boolean {
        return Messages.showOkCancelDialog(
            message("ui.courseProject.dialogs.showRestartDialog.message"),
            message("ui.courseProject.dialogs.showRestartDialog.title"),
            message("ui.courseProject.dialogs.showRestartDialog.okText"),
            message("ui.courseProject.dialogs.showRestartDialog.cancelText"),
            Messages.getQuestionIcon()
        ) == Messages.OK
    }
}
