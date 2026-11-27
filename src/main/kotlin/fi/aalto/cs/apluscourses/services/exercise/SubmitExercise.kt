package fi.aalto.cs.apluscourses.services.exercise

import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportSequentialProgress
import com.intellij.serialization.PropertyMapping
import com.intellij.util.application
import fi.aalto.cs.aplus.client.AplusException
import fi.aalto.cs.aplus.client.dto.LocalizedFileField
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.Submission
import fi.aalto.cs.apluscourses.model.mapping.toGroups
import fi.aalto.cs.apluscourses.model.mapping.toSubmissionFiles
import fi.aalto.cs.apluscourses.model.people.Group
import fi.aalto.cs.apluscourses.notifications.*
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.Notifier
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.ui.exercise.DuplicateSubmissionDialog
import fi.aalto.cs.apluscourses.ui.exercise.SubmitExerciseDialog
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import fi.aalto.cs.apluscourses.utils.FileUtil
import kotlinx.coroutines.*
import org.jetbrains.annotations.NonNls
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicLong
import kotlin.io.path.Path
import kotlin.io.path.invariantSeparatorsPathString

internal suspend fun saveSubmissionDocuments() {
    withContext(Dispatchers.EDT) {
        FileDocumentManager.getInstance().saveAllDocuments()
    }
}

@Service(Service.Level.PROJECT)
class SubmitExercise(
    private val project: Project,
    private val cs: CoroutineScope
) {
    private val submissionFields = mutableMapOf<Long, Map<String, List<LocalizedFileField>>>()
    private val submittingExerciseId = AtomicLong(NO_SUBMISSION)

    fun isSubmitting(exercise: Exercise): Boolean = submittingExerciseId.get() == exercise.id

    fun submit(exercise: Exercise) {
        cs.launch {
            if (!submittingExerciseId.compareAndSet(NO_SUBMISSION, exercise.id)) {
                CoursesLogger.warn("Already submitting exercise")
                return@launch
            }
            fireExerciseUpdated(exercise)
            try {
                CoursesLogger.info("Submitting $exercise")

                val prepared = withBackgroundProgress(
                    project,
                    message("services.progress.preparingSubmission")
                ) {
                    prepare(exercise)
                } ?: return@launch

                val submissionDialog = withContext(Dispatchers.EDT) {
                    SubmitExerciseDialog(
                        project,
                        exercise,
                        prepared.files.values.toList(),
                        prepared.groups,
                        prepared.group,
                        prepared.submittedBefore
                    )
                }
                if (withContext(Dispatchers.EDT) { !submissionDialog.showAndGet() }) {
                    return@launch
                }

                val submission = Submission(
                    exercise,
                    prepared.files,
                    submissionDialog.selectedGroup.get(),
                    prepared.language
                )
                val result = withBackgroundProgress(
                    project,
                    message("services.progress.submitting", exercise.name)
                ) {
                    CoursesClient.getInstance(project).aplus.submit(
                        exerciseId = exercise.id,
                        files = withContext(Dispatchers.IO) { toSubmissionFiles(submission.files) },
                        groupId = submission.group.id,
                        language = submission.language
                    )
                }
                if (result.errors.isNotEmpty()) notifier.notify(SubmissionErrorsNotification(result.errors), project)
                ExercisesUpdater.getInstance(project).restart()

                DuplicateSubmissionChecker.getInstance(project)
                    .onAssignmentSubmitted(exercise.id, prepared.files)

                CoursesLogger.info("Submitted exercise $exercise successfully")
            } catch (ex: IOException) {
                CoursesLogger.error("Network error in SubmitExercise", ex)
                notifyNetworkError(ex, project)
            } catch (ex: AplusException) {
                CoursesLogger.error("Network error in SubmitExercise", ex)
                notifyNetworkError(ex, project)
            } catch (ex: FileDoesNotExistException) {
                notifier.notify(MissingFileNotification(ex.path, ex.name), project)
            } catch (_: ModuleMissingException) {
                notifier.notify(MissingModuleNotification(), project)
            } finally {
                // While an id is set, the plugin refuses every other submission.
                submittingExerciseId.set(NO_SUBMISSION)
                fireExerciseUpdated(exercise)
                CoursesLogger.debug("Finished submitting exercise")
            }
        }
    }

    // Repaints the exercise's tree rows so the "New submission" icon reflects isSubmitting
    private fun fireExerciseUpdated(exercise: Exercise) {
        application.invokeLater {
            project.messageBus
                .syncPublisher(ExercisesUpdater.EXERCISES_TOPIC)
                .onExerciseUpdated(exercise)
        }
    }

    private class Prepared(
        val files: Map<String, Path>,
        val groups: List<Group>,
        val group: Group,
        val language: String,
        val submittedBefore: Boolean
    )

    /**
     * Gathers the assignment details, the files to submit and the groups available to submit with.
     *
     * Returns null when the submission cannot go ahead and the reason has already been reported.
     */
    private suspend fun prepare(exercise: Exercise): Prepared? = reportSequentialProgress { reporter ->
        reporter.nextStep(DETAILS_LOADED, message("services.progress.submission.details"))

        val fieldsByLanguage = submissionFields.getOrPut(exercise.id) {
            CoursesClient.getInstance(project).aplus.exercise(exercise.id).exerciseInfo?.fileFieldsByLanguage()
                .orEmpty()
        }

        val submittedBefore = exercise.submissionResults.isNotEmpty()
        val submittersFromBefore = exercise.submissionResults.firstOrNull()?.submitters

        CoursesLogger.info("Has exercise been submitted before: $submittedBefore, submitters: $submittersFromBefore")

        cs.ensureActive()

        val language: String = CourseFileManager.getInstance(project).state.language!!
        CoursesLogger.info("Language: $language")

        val requestedFiles = fieldsByLanguage[language].orEmpty()
        if (requestedFiles.isEmpty()) {
            CoursesLogger.warn("$exercise not submittable")
            notifier.notify(NotSubmittableNotification(), project)
            return null
        }

        val course = CourseManager.course(project)
        if (course == null) {
            CoursesLogger.error("Course not found")
            return null
        }

        val module = exercise.module
        val platformModule = module?.platformObject

        if (module == null || platformModule == null) {
            CoursesLogger.error("Module $module not found")
            throw ModuleMissingException()
        }

        reporter.nextStep(FILES_COLLECTED, message("services.progress.submission.files"))

        saveSubmissionDocuments()

        var modulePath = Path(ModuleUtilCore.getModuleDirPath(platformModule))
        if (modulePath.endsWith(Path(SBT_MODULE_DIR))) {
            // in the case of SBT modules, the .iml file is inside .idea/modules, so we need to
            // go up the directory tree two times to reach the project directory
            modulePath = modulePath.parent.parent
        } else if (modulePath.endsWith(Path(SBT_MODULE_DIR).resolve(module.name))) {
            // in newer versions the module name is included in the path
            modulePath = modulePath.parent.parent.parent
        }
        CoursesLogger.info("Detected module: $module, path: $modulePath")

        val files: MutableMap<String, Path> = HashMap()
        for ((key, name) in requestedFiles) {
            files[key] = FileUtil.findFileInDirectory(modulePath.invariantSeparatorsPathString, name)
            { file ->
                !file.invariantSeparatorsPath.startsWith("${module.fullPath.resolve(Module.BACKUP_DIR).invariantSeparatorsPathString}/")
            } ?: throw FileDoesNotExistException(
                modulePath.invariantSeparatorsPathString,
                name
            )
        }
        CoursesLogger.info("Submission files: $files")

        if (withContext(Dispatchers.EDT) {
                (DuplicateSubmissionChecker.getInstance(project).isDuplicateSubmission(exercise.id, files)
                        && !DuplicateSubmissionDialog.showDialog()
                        )
            }) {
            CoursesLogger.info("Duplicate submission detected and user chose to cancel")
            return null
        }

        reporter.nextStep(GROUPS_LOADED, message("services.progress.submission.groups"))

        val groups = listOf(Group.SUBMIT_ALONE) +
                toGroups(CoursesClient.getInstance(project).aplus.myGroups(course.id))

        // Find the group from the available groups that matches the default group ID.
        // A group could be removed, so this way we check that the default group ID is still valid.
        val defaultGroupId = CourseFileManager.getInstance(project).state.defaultGroupId
        var group = groups.find { it.id == defaultGroupId }
        if (group == null) {
            group = Group.SUBMIT_ALONE
            CourseFileManager.getInstance(project).setDefaultGroup(group)
        }
        if (submittedBefore) {
            val submitters = submittersFromBefore ?: emptyList()
            group = groups.find { candidate ->
                submitters.containsAll(candidate.members.map { it.id })
            } ?: Group.SUBMIT_ALONE
        }

        CoursesLogger.info("Selected group: $group, default group: $defaultGroupId")

        Prepared(files, groups, group, language, submittedBefore)
    }

    private fun notifyNetworkError(exception: Exception, project: Project) {
        CoursesLogger.warn("Network error while submitting exercise", exception)
        notifier.notify(NetworkErrorNotification(exception), project)
    }

    companion object {
        /** No exercise id is negative, so this marks "no submission in progress". */
        private const val NO_SUBMISSION = -1L

        class ModuleMissingException @PropertyMapping() constructor() : Exception() {
            companion object {
                private const val serialVersionUID: Long = 1L
            }
        }

        class FileDoesNotExistException @PropertyMapping("path", "name") constructor(
            val path: String,
            val name: String
        ) : Exception() {
            companion object {
                private const val serialVersionUID: Long = 1L
            }
        }

        @NonNls
        private const val SBT_MODULE_DIR = ".idea/modules"

        private val notifier = Notifier

        private const val DETAILS_LOADED = 30
        private const val FILES_COLLECTED = 60
        private const val GROUPS_LOADED = 100
    }
}
