package fi.aalto.cs.apluscourses.services.exercise

import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.serialization.PropertyMapping
import fi.aalto.cs.apluscourses.api.APlusApi
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.Submission
import fi.aalto.cs.apluscourses.model.exercise.SubmissionInfo
import fi.aalto.cs.apluscourses.model.people.Group
import fi.aalto.cs.apluscourses.notifications.MissingFileNotification
import fi.aalto.cs.apluscourses.notifications.MissingModuleNotification
import fi.aalto.cs.apluscourses.notifications.NetworkErrorNotification
import fi.aalto.cs.apluscourses.notifications.NotSubmittableNotification
import fi.aalto.cs.apluscourses.services.Notifier
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.ui.exercise.DuplicateSubmissionDialog
import fi.aalto.cs.apluscourses.ui.exercise.SubmitExerciseDialog
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import fi.aalto.cs.apluscourses.utils.FileUtil
import kotlinx.coroutines.*
import java.io.IOException
import java.nio.file.Path

@Service(Service.Level.PROJECT)
class SubmitExercise(
    private val project: Project,
    private val cs: CoroutineScope
) {
    private val submissionInfos: MutableMap<Long, SubmissionInfo> = HashMap()

    fun submit(exercise: Exercise) {
        cs.launch {
            try {
                CoursesLogger.info("Submitting $exercise")
                var submissionInfo = submissionInfos[exercise.id]
                if (submissionInfo == null) {
                    submissionInfo = SubmissionInfo.fromJsonObject(APlusApi.exercise(exercise).get(project))
                    submissionInfos[exercise.id] = submissionInfo
                }

                val submittedBefore = exercise.submissionResults.isNotEmpty()
                val submittersFromBefore = exercise.submissionResults.firstOrNull()?.submitters

                CoursesLogger.info("Has exercise been submitted before: $submittedBefore, submitters: $submittersFromBefore")

                cs.ensureActive()

                val language: String = CourseFileManager.getInstance(project).state.language!!
                CoursesLogger.info("Language: $language")
                val submittable = submissionInfo.isSubmittable(language)

                if (!submittable) {
                    CoursesLogger.warn("$exercise not submittable")
                    notifier.notify(NotSubmittableNotification(), project)
                    return@launch
                }

                val course = CourseManager.course(project)
                if (course == null) {
                    CoursesLogger.error("Course not found")
                    return@launch
                }

                val module = exercise.module
                val platformModule = module?.platformObject

                if (module == null || platformModule == null) {
                    CoursesLogger.error("Module $module not found")
                    throw ModuleMissingException()
                }

                withContext(Dispatchers.EDT) {
                    FileDocumentManager.getInstance().saveAllDocuments()
                }

                val modulePath = ModuleUtilCore.getModuleDirPath(platformModule)

                CoursesLogger.info("Detected module: $module, path: $modulePath")

                val files: MutableMap<String, Path> = HashMap()
                for ((key, name) in submissionInfo.getFiles(language)) {
                    files[key] = FileUtil.findFileInDirectory(modulePath, name) ?: throw FileDoesNotExistException(
                        modulePath,
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
                    return@launch
                }

                val groups = listOf(Group.GROUP_ALONE) + APlusApi.course(course).myGroups(project)

                // Find the group from the available groups that matches the default group ID.
                // A group could be removed, so this way we check that the default group ID is still valid.
                var defaultGroupId = CourseFileManager.getInstance(project).state.defaultGroupId
                var group = groups.find { it.id == defaultGroupId }
                if (group == null) {
                    group = Group.GROUP_ALONE
                    CourseFileManager.getInstance(project).setDefaultGroup(group)
                }
                if (submittedBefore) {
                    val submitters = submittersFromBefore ?: emptyList()
                    group = groups.find {
                        submitters.containsAll(it.members.map { it.id })
                    } ?: Group.GROUP_ALONE
                }

                CoursesLogger.info("Selected group: $group, default group: $defaultGroupId")

                val submissionDialog = withContext(Dispatchers.EDT) {
                    SubmitExerciseDialog(project, exercise, files.values.toList(), groups, group, submittedBefore)
                }

                if (withContext(Dispatchers.EDT) {
                        !submissionDialog.showAndGet()
                    }) {
                    return@launch
                }

                val submission = Submission(exercise, files, submissionDialog.selectedGroup.get(), language)

                APlusApi.exercise(exercise).submit(submission, project)
                ExercisesUpdater.getInstance(project).restart()

                DuplicateSubmissionChecker.getInstance(project)
                    .onAssignmentSubmitted(exercise.id, files)

                CoursesLogger.info("Submitted exercise $exercise successfully")
            } catch (ex: IOException) {
                notifyNetworkError(ex, project)
            } catch (ex: FileDoesNotExistException) {
                notifier.notify(MissingFileNotification(ex.path, ex.name), project)
            } catch (_: ModuleMissingException) {
                notifier.notify(MissingModuleNotification(), project)
            }
            CoursesLogger.debug("Finished submitting exercise")
        }

    }

    private fun notifyNetworkError(exception: IOException, project: Project) {
        CoursesLogger.warn("Network error while submitting exercise", exception)
        notifier.notify(NetworkErrorNotification(exception), project)
    }

    companion object {
        class ModuleMissingException @PropertyMapping() constructor() : Exception() {
            private val serialVersionUID: Long = 1L
        }

        class FileDoesNotExistException @PropertyMapping("path", "name") constructor(
            val path: String,
            val name: String
        ) : Exception() {
            private val serialVersionUID: Long = 1L
        }

        private val notifier = Notifier
    }
}