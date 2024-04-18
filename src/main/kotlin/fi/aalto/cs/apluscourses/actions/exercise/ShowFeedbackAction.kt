package fi.aalto.cs.apluscourses.actions.exercise

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project

class ShowFeedbackAction : AnAction() {
//    private val mainViewModelProvider: MainViewModelProvider = mainViewModelProvider
//
//    private val authenticationProvider: AuthenticationProvider = authenticationProvider
//
//    private var submissionResult: SubmissionResult? = null
//
//    private val notifier: Notifier = notifier

    /**
     * Constructor.
     */

    override fun actionPerformed(e: AnActionEvent) {
//        val project: Project = e.project ?: return
//        ApplicationManager.getApplication().service<ExercisesTreeFilterService>().toggleFilter()
//
//        val mainViewModel: Unit = mainViewModelProvider.getMainViewModel(project)
//        val exercisesViewModel: Unit = mainViewModel.exercisesViewModel.get()
//        val courseViewModel: Unit = mainViewModel.courseViewModel.get()
//        val authentication: Unit = authenticationProvider.getAuthentication(project)

//        if (courseViewModel == null || exercisesViewModel == null || authentication == null) {
//            return
//        }

//        val selection: ExercisesTreeViewModel.ExerciseTreeSelection =
//            exercisesViewModel.findSelected() as ExercisesTreeViewModel.ExerciseTreeSelection
//        val selectedExercise: Unit = selection.getExercise()
//        val selectedSubmission: Unit = selection.getSubmissionResult()
//        if ((selectedExercise == null || selectedSubmission == null) && submissionResult == null) {
//            return
//        }
//        if (submissionResult == null) {
//            submissionResult = selectedSubmission.getModel()
//        }
//        val course: Unit = courseViewModel.getModel()
//        val exerciseDataSource: Unit = course.getExerciseDataSource()
//
//        val progress: Unit = mainViewModel.progressViewModel.start(1, "Loading Feedback...", true)
//        try {
//            val feedbackCss: Unit = mainViewModel.feedbackCss ?: return
//
//            val feedbackString: Unit = exerciseDataSource.getSubmissionFeedback(submissionResult.id, authentication)
//            val document = Jsoup.parseBodyFragment(feedbackString)
//
//            val textColor: JBColor = JBColor.black
//            val textColorString =
//                String.format("#%02x%02x%02x", textColor.getRed(), textColor.getGreen(), textColor.getBlue())
//            val backgroundColor: Color = JBColor.background()
//            val backgroundColorString = String.format(
//                "#%02x%02x%02x", backgroundColor.red, backgroundColor.green,
//                backgroundColor.blue
//            )
//            val fontName: String = JBFont.regular().getFontName()
//
//            document.head().append(
//                "<style>"
//                        + Jsoup.clean(
//                    feedbackCss
//                        .replaceAll("TEXT_COLOR", textColorString)
//                        .replaceAll("BG_COLOR", backgroundColorString)
//                        .replaceAll("FONT_NAME", fontName),
//                    Safelist.none()
//                )
//                        + "</style>"
//            )
//
//            val fileEditorManager: FileEditorManager = FileEditorManager.getInstance(project)
//
//            Arrays.stream<FileEditor>(fileEditorManager.getAllEditors())
//                .filter(Predicate<FileEditor> { editor: FileEditor ->
//                    editor.getFile() != null && editor.getFile().getName().startsWith("Feedback for ")
//                })
//                .findFirst()
//                .ifPresent(Consumer<FileEditor> { editor: FileEditor ->
//                    fileEditorManager.closeFile(
//                        Objects.requireNonNull<VirtualFile>(
//                            editor.getFile()
//                        )
//                    )
//                })
//            openEditor(
//                project, getAndReplaceText(
//                    "ui.ShowFeedbackAction.feedbackTitle",
//                    submissionResult.getExercise().name, submissionResult.id.toString()
//                ),
//                document.html()
//            )
//        } catch (ex: IOException) {
//            notifier.notify(NetworkErrorNotification(ex), project)
//        }
//        submissionResult = null
//        progress.finish()
    }

    override fun update(e: AnActionEvent) {
        val project: Project = e.project ?: return
//
//        val mainViewModel: Unit = mainViewModelProvider.getMainViewModel(project)
//        val exercisesViewModel: Unit = mainViewModel.exercisesViewModel.get()
//        val courseViewModel: Unit = mainViewModel.courseViewModel.get()
//
//        if (courseViewModel == null || exercisesViewModel == null || mainViewModel.feedbackCss == null) {
//            e.getPresentation().setVisible(false)
//            return
//        }
//        val selectedSubmissionResult: Unit =
//            (exercisesViewModel.findSelected() as ExercisesTreeViewModel.ExerciseTreeSelection).getSubmissionResult()
//        if (selectedSubmissionResult == null || !selectedSubmissionResult.getModel().getExercise().isSubmittable()) {
//            e.getPresentation().setEnabled(false)
//        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
//
//    fun setSubmissionResult(submissionResult: SubmissionResult?) {
//        this.submissionResult = submissionResult
//    }
//
//    companion object {
//        val ACTION_ID: String = ShowFeedbackAction::class.java.canonicalName
//    }
}