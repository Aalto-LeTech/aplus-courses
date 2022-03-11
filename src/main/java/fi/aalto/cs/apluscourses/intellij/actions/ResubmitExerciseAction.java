package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.notifications.SubmissionSentNotification;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.Interfaces;
import fi.aalto.cs.apluscourses.model.SubmissionStatusUpdater;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionResultViewModel;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public class ResubmitExerciseAction extends AnAction {
  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final Interfaces.AuthenticationProvider authenticationProvider;

  @NotNull
  private final Notifier notifier;

  @NotNull
  private final Interfaces.AssistantModeProvider assistantModeProvider;

  /**
   * Constructor with reasonable defaults.
   */
  public ResubmitExerciseAction() {
    this(
        PluginSettings.getInstance(),
        project -> Optional.ofNullable(PluginSettings.getInstance().getCourseProject(project))
            .map(CourseProject::getAuthentication).orElse(null),
        new DefaultNotifier(),
        () -> PluginSettings.getInstance().isAssistantMode()
    );
  }

  /**
   * Constructor.
   */
  public ResubmitExerciseAction(@NotNull MainViewModelProvider mainViewModelProvider,
                                @NotNull Interfaces.AuthenticationProvider authenticationProvider,
                                @NotNull Notifier notifier,
                                @NotNull Interfaces.AssistantModeProvider assistantModeProvider) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.authenticationProvider = authenticationProvider;
    this.notifier = notifier;
    this.assistantModeProvider = assistantModeProvider;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    var project = e.getProject();
    if (project == null) {
      return;
    }
    var mainViewModel = mainViewModelProvider.getMainViewModel(project);
    var courseViewModel = mainViewModel.courseViewModel.get();
    var exercisesViewModel = mainViewModel.exercisesViewModel.get();
    var authentication = authenticationProvider.getAuthentication(project);
    if (courseViewModel == null || exercisesViewModel == null || authentication == null) {
      return;
    }

    var selectedItem = (ExercisesTreeViewModel.ExerciseTreeSelection) exercisesViewModel.findSelected();
    var selectedExercise = selectedItem.getExercise();
    var selectedSubmissionResult = selectedItem.getSubmissionResult();
    if (selectedExercise == null || selectedSubmissionResult == null) {
      return;
    }
    if (JOptionPane.showConfirmDialog(null,
        "Are you sure you want to re-submit " + selectedExercise.getPresentableName() + " "
            + selectedSubmissionResult.getPresentableName(),
        "Re-Submit",
        JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
      return;
    }

    var course = courseViewModel.getModel();
    var submissionUrl = course.getApiUrl() + "submissions/" + selectedSubmissionResult.getModel().getId();
    var exerciseDataSource = course.getExerciseDataSource();

    try {
      CoursesClient.post(new URL(selectedSubmissionResult.getModel().getHtmlUrl() + "re-submit/"), authentication,
          null, a -> null);
      new SubmissionStatusUpdater(
          project, exerciseDataSource, authentication, submissionUrl, selectedExercise.getModel(), course
      ).start();
      notifier.notifyAndHide(new SubmissionSentNotification(), project);
    } catch (IOException ex) {
      notifier.notify(new NetworkErrorNotification(ex), project);
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    if (!assistantModeProvider.isAssistantMode()) {
      e.getPresentation().setVisible(false);
      return;
    }
    var project = e.getProject();
    var mainViewModel = mainViewModelProvider.getMainViewModel(project);
    var courseViewModel = mainViewModel.courseViewModel.get();
    var exercisesViewModel = mainViewModel.exercisesViewModel.get();
    var authentication = authenticationProvider.getAuthentication(project);
    if (exercisesViewModel == null) {
      e.getPresentation().setEnabled(false);
    } else {
      var selectedItem = exercisesViewModel.getSelectedItem();
      var isSubmittableSubmissionSelected = selectedItem instanceof SubmissionResultViewModel
          && ((SubmissionResultViewModel) selectedItem).getModel().getExercise()
          .isSubmittable();
      e.getPresentation().setEnabled(project != null
          && authentication != null && courseViewModel != null
          && isSubmittableSubmissionSelected);
    }
    if ((ActionPlaces.TOOLWINDOW_POPUP).equals(e.getPlace()) && !e.getPresentation().isEnabled()) {
      e.getPresentation().setVisible(false);
    }
  }
}
