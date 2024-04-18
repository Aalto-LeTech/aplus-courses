package fi.aalto.cs.apluscourses.actions.exercise

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import fi.aalto.cs.apluscourses.services.exercise.SelectedExerciseService

class SubmitExerciseAction : AnAction() {
//    private val mainViewModelProvider: MainViewModelProvider = mainViewModelProvider
//
//    private val authenticationProvider: AuthenticationProvider = authenticationProvider
//
//    private val fileFinder: FileFinder = fileFinder
//
//    private val moduleSource: ProjectModuleSource = moduleSource
//
//    private val dialogs: Dialogs = dialogs
//
//    private val notifier: Notifier = notifier
//
//    private val tagger: Interfaces.Tagger = tagger
//
//    private val documentSaver: Interfaces.DocumentSaver = documentSaver
//
//    // TODO: store language and default group ID in the object model and read them from there
//    private val languageSource: Interfaces.LanguageSource = languageSource
//
//    private val defaultGroupIdSetting: DefaultGroupIdSetting = defaultGroupIdSetting
//
//    private val moduleDirGuesser: Interfaces.ModuleDirGuesser = moduleDirGuesser
//
//    private val duplicateChecker: Interfaces.DuplicateSubmissionChecker = duplicateChecker
//
//    private val groupSelector: Interfaces.SubmissionGroupSelector = groupSelector

    /**
     * Construct an exercise submission action with the given parameters. This constructor is useful
     * for testing purposes.
     */

    override fun update(e: AnActionEvent) {
        val selectedExercise = e.project?.service<SelectedExerciseService>()?.selectedExercise
        e.presentation.isEnabled = selectedExercise != null && selectedExercise.isSubmittable
//        val project: Project = e.getProject()
//        val mainViewModel: MainViewModel = mainViewModelProvider.getMainViewModel(project)
//        val courseViewModel: CourseViewModel = mainViewModel.courseViewModel.get()
//        val exercisesViewModel: ExercisesTreeViewModel = mainViewModel.exercisesViewModel.get()
//        val authentication: Authentication = authenticationProvider.getAuthentication(project)
//        val progressViewModel: ProgressViewModel = mainViewModel.progressViewModel
//        if (exercisesViewModel == null) {
//            e.getPresentation().setEnabled(false)
//        } else {
//            val selectedItem: Unit = exercisesViewModel.getSelectedItem()
//            val isSubmittableExerciseSelected = (selectedItem is ExerciseViewModel
//                    && (selectedItem as ExerciseViewModel).isSubmittable())
//            val isSubmittableSubmissionSelected = (selectedItem is SubmissionResultViewModel
//                    && (selectedItem as SubmissionResultViewModel).getModel().getExercise()
//                .isSubmittable())
//            e.getPresentation()
//                .setEnabled(project != null && authentication != null && courseViewModel != null && (isSubmittableExerciseSelected || isSubmittableSubmissionSelected))
//            if (progressViewModel.getCurrentProgress() != null && !progressViewModel.getCurrentProgress()
//                    .isFinished()
//            ) {
//                e.getPresentation().setText(getText("intellij.actions.SubmitExerciseAction.waitForAssignments"))
//            } else {
//                e.getPresentation().setText(getText("intellij.actions.SubmitExerciseAction.submitAssignment"))
//            }
//            e.getPresentation().setVisible(true)
//        }
//        if ((ActionPlaces.TOOLWINDOW_POPUP) == e.getPlace() && !e.getPresentation().isEnabled()) {
//            e.getPresentation().setVisible(false)
//        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
//        logger.debug("Starting SubmitExerciseAction")
//        val project: Project = e.getProject() ?: return
//        try {
//            trySubmit(project)
//        } catch (ex: IOException) {
//            notifyNetworkError(ex, project)
//        } catch (ex: FileDoesNotExistException) {
//            notifier.notify(MissingFileNotification(ex.getPath(), ex.getName()), project)
//        } catch (ex: ModuleMissingException) {
//            notifier.notify(MissingModuleNotification(ex.getModuleName()), project)
//        }
    }
//
//    @Throws(IOException::class, FileDoesNotExistException::class, ModuleMissingException::class)
//    private fun trySubmit(project: Project) {
//        val mainViewModel: MainViewModel = mainViewModelProvider.getMainViewModel(project)
//        val courseViewModel: CourseViewModel = mainViewModel.courseViewModel.get()
//        val exercisesViewModel: ExercisesTreeViewModel = mainViewModel.exercisesViewModel.get()
//        val authentication: Authentication = authenticationProvider.getAuthentication(project)
//
//        if (courseViewModel == null || exercisesViewModel == null || authentication == null) {
//            if (authentication == null) {
//                logger.warn("Null authentication while submitting exercise")
//            }
//            return
//        }
//
//        val selection: ExercisesTreeViewModel.ExerciseTreeSelection =
//            exercisesViewModel.findSelected() as ExercisesTreeViewModel.ExerciseTreeSelection
//        val selectedExercise: ExerciseViewModel = selection.getExercise()
//        val selectedExerciseGroup: ExerciseGroupViewModel = selection.getExerciseGroup()
//        if (selectedExercise == null || selectedExerciseGroup == null) {
//            notifier.notifyAndHide(ExerciseNotSelectedNotification(), project)
//            return
//        }
//
//        val exercise: Exercise = selectedExercise.getModel()
//        logger.info("Submitting {}", exercise)
//        val submissionInfo: SubmissionInfo? = exercise.submissionInfo
//        val language: String = languageSource.getLanguage(project)
//        logger.info("Language: {}", language)
//
//        if (!submissionInfo.isSubmittable(language)) {
//            logger.warn("{} not submittable", exercise)
//            notifier.notify(NotSubmittableNotification(), project)
//            return
//        }
//
//        val exerciseModules: Map<String, String> =
//            courseViewModel.getModel().getExerciseModules().get(exercise.id)
//
//        val moduleName = Optional
//            .ofNullable(exerciseModules)
//            .map { self: Map<String, String> -> self[language] }
//
//        val selectedModule: Module?
//        var moduleDir: VirtualFile? = null
//        if (moduleName.isPresent) {
//            selectedModule = moduleName
//                .map(Function<String?, Any> { self: String? -> moduleSource.getModule(project, self) })
//                .orElseThrow(Supplier { ModuleMissingException(moduleName.get()) })
//        } else {
//            val modules: Array<Module> = moduleSource.getModules(project)
//
//            val moduleSelectionViewModel: ModuleSelectionViewModel = ModuleSelectionViewModel(
//                modules, getText("ui.toolWindow.subTab.exercises.submission.selectModule"), project, moduleDirGuesser
//            )
//            if (!dialogs.create(moduleSelectionViewModel, project).showAndGet()) {
//                return
//            }
//            selectedModule = moduleSelectionViewModel.selectedModule.get()
//            moduleDir = moduleSelectionViewModel.selectedModuleFile.get()
//        }
//
//        logger.info("Selected {}", selectedModule)
//
//        if (selectedModule == null) {
//            return
//        }
//
//        documentSaver.saveAllDocuments()
//
//        val modulePath =
//            if (moduleDir == null) Paths.get(ModuleUtilCore.getModuleDirPath(selectedModule)) else Paths.get(moduleDir.path)
//        val files: MutableMap<String, Path> = HashMap()
//        for (file in submissionInfo.getFiles(language)) {
//            files[file.key] = fileFinder.findFile(modulePath, file.name)
//        }
//        logger.info("Submission files: {}", files)
//
//        val course: Unit = courseViewModel.getModel()
//        val exerciseDataSource: Unit = course.getExerciseDataSource()
//
//        val groups: List<Group?> = ArrayList<Any?>(exerciseDataSource.getGroups(course, authentication))
//        groups.add(0, Group.GROUP_ALONE)
//
//        // Find the group from the available groups that matches the default group ID.
//        // A group could be removed, so this way we check that the default group ID is still valid.
//        val defaultGroupId: Optional<Long> = defaultGroupIdSetting.getDefaultGroupId()
//        val defaultGroup: Group = defaultGroupId
//            .flatMap<Group>(Function<Long, Optional<out Group?>> { id: Long ->
//                groups
//                    .stream()
//                    .filter(Predicate<Group?> { group: Group? -> group.getId() === id })
//                    .findFirst()
//            })
//            .orElse(null)
//
//        val lastSubmittedGroupId: String =
//            groupSelector.getLastSubmittedGroupId(project, course.getId(), exercise.id)
//        val lastSubmittedGroup: Group? = groups
//            .stream()
//            .filter(Predicate<Group?> { g: Group? -> g.getMemberwiseId().equals(lastSubmittedGroupId) })
//            .findFirst()
//            .orElse(null)
//
//        val submission: SubmissionViewModel = SubmissionViewModel(
//            exercise, groups, defaultGroup,
//            lastSubmittedGroup, files, language
//        )
//
//        if (!dialogs.create(submission, project).showAndGet()) {
//            return
//        }
//
//        if (duplicateChecker.isDuplicateSubmission(project, course.getId(), exercise.id, files)
//            && !DuplicateSubmissionDialog.showDialog()
//        ) {
//            return
//        }
//
//        val selectedGroup: Group = Objects.requireNonNull(submission.selectedGroup.get())
//
//        if (Boolean.TRUE == submission.makeDefaultGroup.get()) {
//            defaultGroupIdSetting.setDefaultGroupId(selectedGroup.getId())
//        } else {
//            defaultGroupIdSetting.clearDefaultGroupId()
//        }
//
//        logger.info("Submitting with group: {}", selectedGroup)
//        val submissionUrl: String = exerciseDataSource.submit(submission.buildSubmission(), authentication)
//        logger.info("Submission url: {}", submissionUrl)
//
//        groupSelector.onAssignmentSubmitted(project, course.getId(), exercise.id, selectedGroup)
//        duplicateChecker.onAssignmentSubmitted(project, course.getId(), exercise.id, files)
//
//        SubmissionStatusUpdater(
//            project, exerciseDataSource, authentication, submissionUrl, selectedExercise.getModel(), course
//        ).start()
//        notifier.notifyAndHide(SubmissionSentNotification(), project)
//
//        val tag: String = getAndReplaceText(
//            "ui.localHistory.submission.tag",
//            selectedExerciseGroup.getPresentableName(),
//            submission.getPresentableExerciseName(),
//            submission.getCurrentSubmissionNumber()
//        )
//        addLocalHistoryTag(project, tag)
//        logger.debug("Finished submitting exercise")
//    }
//
//    private fun notifyNetworkError(exception: IOException, project: Project?) {
//        logger.warn("Network error while submitting exercise", exception)
//        notifier.notify(NetworkErrorNotification(exception), project)
//    }
//
//    private fun addLocalHistoryTag(project: Project, tag: String) {
//        tagger.putSystemLabel(project, tag, ACCENT_COLOR)
//    }
//
//    private class ModuleMissingException(val moduleName: String) : Exception()
//    companion object {
//        private val logger: Logger = APlusLogger.logger
//
//        val ACTION_ID: String = SubmitExerciseAction::class.java.canonicalName
//    }
}
