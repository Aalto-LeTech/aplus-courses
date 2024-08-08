package fi.aalto.cs.apluscourses.ui.temp.presentation.exercise

import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.model.temp.Group
import fi.aalto.cs.apluscourses.model.temp.Submission
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.SubmittableFile
import fi.aalto.cs.apluscourses.utils.APlusLogger
import fi.aalto.cs.apluscourses.utils.temp.FileDateFormatter
import java.io.IOException
import java.lang.StringBuilder
import java.nio.file.Path

class SubmissionViewModel(
    val exercise: Exercise,
    val availableGroups: List<Group>,
    val defaultGroup: Group?,
    val lastSubmittedGroup: Group?,
    val filePaths: Map<String, Path>,
    val language: String
) {

    private val submittableFiles: List<SubmittableFile> =
        exercise.submissionInfo!!.getFiles(language)


    //  public final ObservableProperty<Group> selectedGroup =
    //      new ObservableReadWriteProperty<>(null, SubmissionViewModel::validateGroupSelection);
    //
    //  public final ObservableProperty<Boolean> makeDefaultGroup =
    //      new ObservableReadWriteProperty<>(false);
    //
    //  @Nullable
    //  private static ValidationError validateGroupSelection(@Nullable Group group) {
    //    return group == null ? new GroupNotSelectedError() : null;
    //  }
    /**
     * Construct a submission view model with the given exercise, groups, authentication, and project.
     */
    init {
//        this.exercise = exercise
//        this.availableGroups = availableGroups
//        this.lastSubmittedGroup = lastSubmittedGroup
//        this.filePaths = filePaths
//        this.language = language
//        this.submittableFiles = exercise.submissionInfo!!.getFiles(language).toTypedArray<SubmittableFile?>()
        //    if (defaultGroup != null) {
//      selectedGroup.set(defaultGroup);
//      makeDefaultGroup.set(true);
//    }
    }

    fun getPresentableExerciseName(): String {
        return exercise.name
    }

    fun getCurrentSubmissionNumber(): Int {
        return exercise.submissionResults.size + 1
    }

    /**
     * Checks if the user is able to submit with the selected group.
     */
    fun isAbleToSubmitWithGroup(selectedGroup: Group?): Boolean {
        // if the selectedGroup is null, there is no selection, so there's nothing to warn the user against
        // if the lastSubmittedGroup is null, there have been no past submissions, so all groups are fine
        return selectedGroup == null || lastSubmittedGroup == null || selectedGroup === lastSubmittedGroup
    }

    /**
     * Returns a string describing which submission the user is about to make and what the submission
     * limit is (if it exists).
     */
    fun getSubmissionCountText(): String {
        val submissionCountText = StringBuilder("You are about to make submission ")
        submissionCountText.append(getCurrentSubmissionNumber())
        if (exercise.maxSubmissions != 0) {
            submissionCountText.append(" out of ")
            submissionCountText.append(exercise.maxSubmissions)
        }
        submissionCountText.append('.')
        return submissionCountText.toString()
    }

    /**
     * Formats a descriptive string for the submission dialog about a submittable file. The string
     * includes the file name and time since file's last modification.
     *
     * @param file The submittable file in question.
     */
    fun getFileInformationText(file: SubmittableFile): String {
        val fileInfoText = StringBuilder(file.name)
        try {
            val lastModificationTime =
                FileDateFormatter.getFileModificationTime(filePaths.get(file.key)!!) // TODO
            fileInfoText.append(" (modified ").append(lastModificationTime).append(")")
        } catch (e: IOException) {
            // in case of an error, don't display the last modification time and continue gracefully
            logger.warn("Failed to retrieve the file's last modification time", e)
        }
        return fileInfoText.toString()
    }

    /**
     * Warning text if max submission number is exceeded or close to be exceeded.
     *
     * @return A warning text or null, if no warning.
     */
    fun getSubmissionWarning(project: Project?): String? {
        if (exercise.maxSubmissions == 0) {
            return null
        }
        val submissionsLeft = exercise.maxSubmissions - exercise.submissionResults.size
        if (submissionsLeft == 1) {
            return MyBundle.message("presentation.submissionViewModel.warning.lastSubmission", project!!)
        }
        if (submissionsLeft <= 0) {
            return MyBundle.message("presentation.submissionViewModel.warning.submissionsExceeded", project!!)
        }
        return null
    }

    fun buildSubmission(): Submission {
        val group = Group.GROUP_ALONE //Objects.requireNonNull(selectedGroup.get());
        return Submission(exercise, filePaths, group, language)
    } //

    //  public static class GroupNotSelectedError implements ValidationError {
    //
    //    @NotNull
    //    @Override
    //    public String getDescription() {
    //      return getText("presentation.submissionViewModel.selectAGroup");
    //    }
    //  }
    companion object {
        private val logger = APlusLogger.logger
    }
}
