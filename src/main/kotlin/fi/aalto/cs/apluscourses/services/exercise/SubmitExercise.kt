package fi.aalto.cs.apluscourses.services.exercise

import com.intellij.history.LocalHistory
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.api.APlusApi
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.Submission
import fi.aalto.cs.apluscourses.model.exercise.SubmissionInfo
import fi.aalto.cs.apluscourses.model.people.Group
import fi.aalto.cs.apluscourses.notifications.MissingModuleNotification
import fi.aalto.cs.apluscourses.notifications.NetworkErrorNotification
import fi.aalto.cs.apluscourses.notifications.NotSubmittableNotification
import fi.aalto.cs.apluscourses.services.Notifier
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.ui.exercise.SubmitExerciseDialog
import fi.aalto.cs.apluscourses.utils.APlusLogger
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
                logger.info("Submitting $exercise")
                var submissionInfo = submissionInfos[exercise.id]
                if (submissionInfo == null) {
                    println("Submission info is null")
                    submissionInfo = SubmissionInfo.fromJsonObject(APlusApi.exercise(exercise).get(project))
                    submissionInfos[exercise.id] = submissionInfo
                } else {
                    println("Submission info is not null")
                }
                val submittedBefore = exercise.submissionResults.isNotEmpty()
                val submittersFromBefore = exercise.submissionResults.firstOrNull()?.submitters

                cs.ensureActive()

                val language: String = CourseFileManager.getInstance(project).state.language!!
                logger.info("Language: $language")
                val submittable = submissionInfo.isSubmittable(language)

                if (!submittable) {
                    logger.warn("$exercise not submittable")
                    // TODO more info
                    notifier.notify(NotSubmittableNotification(), project)
                    return@launch
                }

                val course = CourseManager.course(project)
                if (course == null) {
                    logger.error("Course not found")
                    return@launch
                }

                val exerciseModules: Map<String, Module> = course.exerciseModules[exercise.id]!!
                println(course.exerciseModules)

                val module = exerciseModules[language]
                val platformModule = module?.platformObject

                logger.info("Selected $module")

                if (module == null || platformModule == null) {
                    return@launch
                }

                withContext(Dispatchers.EDT) {
                    FileDocumentManager.getInstance().saveAllDocuments()
                }

                val modulePath = ModuleUtilCore.getModuleDirPath(platformModule)

                val files: MutableMap<String, Path> = HashMap()
                println(modulePath)
                println(submissionInfo.getFiles(language))
                for ((key, name) in submissionInfo.getFiles(language)) {
                    files[key] = FileUtil.findFileInDirectory(modulePath, name)!!
                }
                logger.info("Submission files: $files")

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

                val submissionDialog = withContext(Dispatchers.EDT) {
                    SubmitExerciseDialog(project, exercise, files.values.toList(), groups, group, submittedBefore)
                }

                val canceled = withContext(Dispatchers.EDT) {
                    !submissionDialog.showAndGet()
                }

                if (canceled) {
                    return@launch
                }
                val submission = Submission(exercise, files, submissionDialog.selectedGroup.get(), language)

                withContext(Dispatchers.EDT) {
//                    if (DuplicateSubmissionCheckerImpl().isDuplicateSubmission(project, course.id, exercise.id, files) TODO
//                        && !DuplicateSubmissionDialog.showDialog()
//                    ) {
//                        return@withContext
//                    }
                }


//                logger.info("Submitting with group: $selectedGroup")
                APlusApi.exercise(exercise).submit(submission, project)
                ExercisesUpdater.getInstance(project).restart()
//            val submissionUrl: String = exerciseDataSource.submit(submission.buildSubmission(), authentication)
//            logger.info("Submission url: {}", submissionUrl)

//            groupSelector.onAssignmentSubmitted(project, course.getId(), exercise.id, selectedGroup)
//            duplicateChecker.onAssignmentSubmitted(project, course.getId(), exercise.id, files)

//            val tag: String = getAndReplaceText(
//                "ui.localHistory.submission.tag",
//                selectedExerciseGroup.getPresentableName(),
//                submission.presentableExerciseName,
//                submission.currentSubmissionNumber
//            )
//            addLocalHistoryTag(project, tag)
            } catch (ex: IOException) {
                notifyNetworkError(ex, project)
//            } catch (ex: FileDoesNotExistException) {
//                notifier.notify(MissingFileNotification(ex.path, ex.name), project)
            } catch (ex: ModuleMissingException) {
                notifier.notify(MissingModuleNotification(ex.moduleName), project)
            }
            logger.debug("Finished submitting exercise")
        }

    }

    private fun notifyNetworkError(exception: IOException, project: Project) {
        logger.warn("Network error while submitting exercise", exception)
        notifier.notify(NetworkErrorNotification(exception), project)
    }


    private fun addLocalHistoryTag(project: Project, tag: String) {
        LocalHistory.getInstance().putSystemLabel(project, tag, CoursesIcons.AccentColor.rgb)
    }

    companion object {
        class ModuleMissingException(val moduleName: String) : Exception()

        private val logger = APlusLogger.logger
        private val notifier = Notifier
    }
}