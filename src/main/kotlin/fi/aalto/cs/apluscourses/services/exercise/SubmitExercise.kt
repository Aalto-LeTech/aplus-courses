package fi.aalto.cs.apluscourses.services.exercise

import com.intellij.history.LocalHistory
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import fi.aalto.cs.apluscourses.api.APlusApi
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.temp.FileDoesNotExistException
import fi.aalto.cs.apluscourses.model.temp.Group
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.SubmissionInfo
import fi.aalto.cs.apluscourses.notifications.MissingFileNotification
import fi.aalto.cs.apluscourses.notifications.MissingModuleNotification
import fi.aalto.cs.apluscourses.notifications.NetworkErrorNotification
import fi.aalto.cs.apluscourses.notifications.NotSubmittableNotification
import fi.aalto.cs.apluscourses.ui.temp.presentation.exercise.SubmissionViewModel
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.Notifier
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService.ExerciseDetails
import fi.aalto.cs.apluscourses.ui.exercise.SubmitExerciseDialog
import fi.aalto.cs.apluscourses.utils.APlusLogger
import fi.aalto.cs.apluscourses.utils.FileUtil
import fi.aalto.cs.apluscourses.utils.ProjectModuleSource
import icons.PluginIcons.ACCENT_COLOR
import kotlinx.coroutines.*
import java.io.IOException
import java.nio.file.Path

@Service(Service.Level.PROJECT)
class SubmitExercise(
    private val project: Project,
    private val cs: CoroutineScope
) {
    private val submissionInfos: MutableMap<Long, SubmissionInfo> = HashMap()

    //    // TODO: store language and default group ID in the object model and read them from there
//    private val languageSource: Interfaces.LanguageSource = languageSource
    fun submit(exercise: Exercise) {
        cs.launch {
            try {
                logger.info("Submitting $exercise")
                var submissionInfo = submissionInfos[exercise.id]
                if (submissionInfo == null) {
                    println("Submission info is null")
                    submissionInfo =
                        SubmissionInfo.fromJsonObject(
                            project.service<CoursesClient>().getBody<ExerciseDetails.Exercise>(exercise.url, true)
                        )
                    submissionInfos[exercise.id] = submissionInfo
                } else {
                    println("Submission info is not null")
                }
                if (exercise.submissionResults.isNotEmpty()) {
                    println("multiple submitters:  ${exercise.submissionResults.first().submitters}")
                }
                exercise.submissionInfo = submissionInfo // TODO remove

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

                val module = exerciseModules.get(language)
                val platformModule = module?.platformObject
//        val moduleName = Optional
//            .ofNullable(exerciseModules)
//            .map { self: Map<String, String> -> self[language] }

//                var selectedModule: com.intellij.openapi.module.Module? = null
//                println(moduleName)
//                val moduleDir: VirtualFile? = null
//                if (moduleName != null) {
//                    selectedModule = ProjectModuleSource.getModule(project, moduleName)
//                    if (selectedModule == null) {
//                        throw ModuleMissingException(moduleName)
//                    }
//                } else {
//                    TODO()
//            val modules = ProjectModuleSource.getModules(project)
//
//            val moduleSelectionViewModel: ModuleSelectionViewModel = ModuleSelectionViewModel(
//                modules, getText("ui.toolWindow.subTab.exercises.submission.selectModule"), project, moduleDirGuesser
//            )
//            if (!dialogs.create(moduleSelectionViewModel, project).showAndGet()) {
//                return
//            }
//            selectedModule = moduleSelectionViewModel.selectedModule.get()
//            moduleDir = moduleSelectionViewModel.selectedModuleFile.get()
//                }

                logger.info("Selected $module")

                if (module == null || platformModule == null) {
                    return@launch
                }

                withContext(Dispatchers.EDT) {
                    FileDocumentManager.getInstance().saveAllDocuments()
                }

                val modulePath = ModuleUtilCore.getModuleDirPath(platformModule)
//                    if (moduleDir == null) ModuleUtilCore.getModuleDirPath(module.platformObject) else moduleDir.path

                val files: MutableMap<String, Path> = HashMap()
                for ((key, name) in submissionInfo.getFiles(language)) {
                    files[key] = FileUtil.findFileInDirectory(modulePath, name)!!
                }
                logger.info("Submission files: $files")

//        val course: Unit = courseViewModel.getModel()
//        val exerciseDataSource: Unit = course.getExerciseDataSource()


                //TODO move to service
                val groups = listOf(Group.GROUP_ALONE) + APlusApi.course(course).myGroups()

                // Find the group from the available groups that matches the default group ID.
                // A group could be removed, so this way we check that the default group ID is still valid.
//            val defaultGroupId: Optional<Long> = defaultGroupIdSetting.getDefaultGroupId()
//            val defaultGroup: Group = defaultGroupId
//                .flatMap<Group>(Function<Long, Optional<out Group?>> { id: Long ->
//                    groups
//                        .stream()
//                        .filter(Predicate<Group?> { group: Group? -> group.getId() === id })
//                        .findFirst()
//                })
//                .orElse(null)
                val defaultGroup = groups.first()


//            val lastSubmittedGroupId: String =
//                groupSelector.getLastSubmittedGroupId(project, course.getId(), exercise.id)
//            val lastSubmittedGroup: Group? = groups
//                .stream()
//                .filter(Predicate<Group?> { g: Group? -> g.getMemberwiseId().equals(lastSubmittedGroupId) })
//                .findFirst()
//                .orElse(null)
                val lastSubmittedGroup = null

                val submission = SubmissionViewModel(
                    exercise, groups, defaultGroup,
                    lastSubmittedGroup, files, language
                )


                val canceled = withContext(Dispatchers.EDT) {
                    return@withContext !SubmitExerciseDialog(project, exercise, files.values.toList()).showAndGet()
                }

                if (canceled) {
                    return@launch
                }

                withContext(Dispatchers.EDT) {
//                    if (DuplicateSubmissionCheckerImpl().isDuplicateSubmission(project, course.id, exercise.id, files) TODO
//                        && !DuplicateSubmissionDialog.showDialog()
//                    ) {
//                        return@withContext
//                    }
                }
//                val selectedGroup: Group = submission.selectedGroup.get()!!

//            if (submission.makeDefaultGroup.get() == true) {
//                defaultGroupIdSetting.setDefaultGroupId(selectedGroup.id)
//            } else {
//                defaultGroupIdSetting.clearDefaultGroupId()
//            }

//                logger.info("Submitting with group: $selectedGroup")
                APlusApi.exercise(exercise).submit(submission.buildSubmission())
                ExercisesUpdaterService.getInstance(project).restart()
//            val submissionUrl: String = exerciseDataSource.submit(submission.buildSubmission(), authentication)
//            logger.info("Submission url: {}", submissionUrl)

//            groupSelector.onAssignmentSubmitted(project, course.getId(), exercise.id, selectedGroup)
//            duplicateChecker.onAssignmentSubmitted(project, course.getId(), exercise.id, files)
//
//            SubmissionStatusUpdater(
//                project, exerciseDataSource, authentication, submissionUrl, selectedExercise.getModel(), course
//            ).start()
//            notifier.notifyAndHide(SubmissionSentNotification(), project)
//
//            val tag: String = getAndReplaceText(
//                "ui.localHistory.submission.tag",
//                selectedExerciseGroup.getPresentableName(),
//                submission.presentableExerciseName,
//                submission.currentSubmissionNumber
//            )
//            addLocalHistoryTag(project, tag)
            } catch (ex: IOException) {
                notifyNetworkError(ex, project)
            } catch (ex: FileDoesNotExistException) {
                notifier.notify(MissingFileNotification(ex.path, ex.name), project)
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
        LocalHistory.getInstance().putSystemLabel(project, tag, ACCENT_COLOR)
    }

    companion object {
        class ModuleMissingException(val moduleName: String) : Exception()

        private val logger = APlusLogger.logger
        private val notifier = Notifier
    }
}