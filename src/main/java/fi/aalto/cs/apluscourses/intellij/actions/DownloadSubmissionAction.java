package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.ExerciseNotSelectedNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.Interfaces;
import fi.aalto.cs.apluscourses.intellij.utils.SubmissionDownloader;
import fi.aalto.cs.apluscourses.model.DummySubmissionResult;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionResultViewModel;
import fi.aalto.cs.apluscourses.utils.APlusLogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class DownloadSubmissionAction extends AnAction {

  private static final Logger logger = APlusLogger.logger;

  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final Notifier notifier;

  @NotNull
  private final Interfaces.AssistantModeProvider assistantModeProvider;

  /**
   * Constructor with reasonable defaults.
   */
  public DownloadSubmissionAction() {
    this(
        PluginSettings.getInstance(),
        new DefaultNotifier(),
        () -> PluginSettings.getInstance().isAssistantMode()
    );
  }


  /**
   * Construct an exercise submission action with the given parameters. This constructor is useful
   * for testing purposes.
   */
  public DownloadSubmissionAction(@NotNull MainViewModelProvider mainViewModelProvider,
                                  @NotNull Notifier notifier,
                                  @NotNull Interfaces.AssistantModeProvider assistantModeProvider) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.notifier = notifier;
    this.assistantModeProvider = assistantModeProvider;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    logger.info("Starting DownloadSubmissionAction");
    var project = e.getProject();
    var mainViewModel = mainViewModelProvider.getMainViewModel(project);
    var courseViewModel = mainViewModel.courseViewModel.get();
    var exercisesTreeViewModel = mainViewModel.exercisesViewModel.get();
    if (project == null || courseViewModel == null || exercisesTreeViewModel == null) {
      return;
    }
    var selectedItem = exercisesTreeViewModel.getSelectedItem();
    if (!(selectedItem instanceof SubmissionResultViewModel)) {
      logger.info("Selected item not submission result");
      return;
    }

    var selection = (ExercisesTreeViewModel.ExerciseTreeSelection) exercisesTreeViewModel.findSelected();
    var selectedExercise = selection.getExercise();
    var selectedExerciseGroup = selection.getExerciseGroup();
    if (selectedExercise == null || selectedExerciseGroup == null) {
      notifier.notifyAndHide(new ExerciseNotSelectedNotification(), project);
      return;
    }

    var exercise = selectedExercise.getModel();
    var course = courseViewModel.getModel();

    new SubmissionDownloader().downloadSubmission(project, course, exercise,
        (SubmissionResult) selectedItem.getModel());
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setVisible(assistantModeProvider.isAssistantMode());
    e.getPresentation().setEnabled(false);
    var mainViewModel = mainViewModelProvider.getMainViewModel(e.getProject());
    var courseViewModel = mainViewModel.courseViewModel.get();
    var exercisesTreeViewModel = mainViewModel.exercisesViewModel.get();
    if (courseViewModel != null && exercisesTreeViewModel != null) {
      var selection = (ExercisesTreeViewModel.ExerciseTreeSelection) exercisesTreeViewModel.findSelected();
      ExerciseViewModel selectedExercise = selection.getExercise();
      var selectedItem = exercisesTreeViewModel.getSelectedItem();
      e.getPresentation().setEnabled(selectedItem instanceof SubmissionResultViewModel
          && selectedExercise != null
          && (selectedExercise.isSubmittable() || selectedItem.getModel() instanceof DummySubmissionResult));
    }
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }
}
