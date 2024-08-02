package fi.aalto.cs.apluscourses.ui.exercise

import com.intellij.openapi.ui.Messages
import fi.aalto.cs.apluscourses.MyBundle.message

object DuplicateSubmissionDialog {
    // TODO check if code has been compiled
    /**
     * Displays a dialog asking the user whether they really want to submit a duplicate submissions.
     *
     * @return True if the user wants to proceed with the submission.
     */
    fun showDialog(): Boolean {
        val options =
            arrayOf<String?>(message("ui.duplicateDialog.yesOption"), message("ui.duplicateDialog.noOption"))

        return Messages.showDialog(
            message("ui.duplicateDialog.content"),
            message("ui.duplicateDialog.title"),
            options,
            1,
            null
        ) == 0
    }
}
