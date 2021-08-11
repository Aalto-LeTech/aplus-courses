package fi.aalto.cs.apluscourses.intellij.actions;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.model.task.IntelliJActivityFactory;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.ExerciseNotSelectedNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.notifications.TaskNotifier;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.TutorialExercise;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseGroupViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.ideactivities.TutorialDialogs;
import fi.aalto.cs.apluscourses.presentation.ideactivities.TutorialViewModel;
import fi.aalto.cs.apluscourses.ui.ideactivities.ComponentDatabase;
import java.util.Optional;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class TutorialAction extends AnAction {
  private final @NotNull MainViewModelProvider mainViewModelProvider;
  private final @NotNull TutorialAuthenticationProvider authenticationProvider;
  private final @NotNull Notifier notifier;
  private final @NotNull TutorialDialogs dialogs;

  /**
   * Empty Constructor.
   */
  public TutorialAction() {
    this(PluginSettings.getInstance(), new DefaultNotifier(), project -> {
      var courseProject = PluginSettings.getInstance().getCourseProject(project);
      return courseProject == null ? null : courseProject.getAuthentication();
    }, new DefaultDialogs());
  }

  /**
   * Constructor.
   */
  public TutorialAction(@NotNull MainViewModelProvider mainViewModelProvider,
                        @NotNull Notifier notifier,
                        @NotNull TutorialAuthenticationProvider authenticationProvider,
                        @NotNull TutorialDialogs dialogs) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.notifier = notifier;
    this.authenticationProvider = authenticationProvider;
    this.dialogs = dialogs;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    if (e.getProject() == null) {
      return;
    }
    doTutorial(e.getProject());
  }

  private void doTutorial(@NotNull Project project) {
    MainViewModel mainViewModel = mainViewModelProvider.getMainViewModel(project);
    CourseViewModel courseViewModel = mainViewModel.courseViewModel.get();
    ExercisesTreeViewModel exercisesViewModel = mainViewModel.exercisesViewModel.get();
    Authentication authentication = authenticationProvider.getAuthentication(project);

    if (courseViewModel == null || exercisesViewModel == null || authentication == null) {
      return;
    }

    var selection = (ExercisesTreeViewModel.ExerciseTreeSelection) exercisesViewModel.findSelected();
    ExerciseViewModel selectedExercise = selection.getExercise();
    ExerciseGroupViewModel selectedExerciseGroup = selection.getExerciseGroup();
    if (selectedExercise == null || selectedExerciseGroup == null
            || !ExerciseViewModel.Status.TUTORIAL.equals(selectedExercise.getStatus())) {
      notifier.notifyAndHide(new ExerciseNotSelectedNotification(), project);
      return;
    }
    TutorialExercise tutorialExercise = (TutorialExercise) selectedExercise.getModel();

    Optional.ofNullable(mainViewModel.tutorialViewModel.get())
        .ifPresent(TutorialViewModel::cancelTutorial);

    TaskNotifier taskNotifier = new TaskNotifier(notifier, project);

    TutorialViewModel tutorialViewModel =
        new TutorialViewModel(tutorialExercise, new IntelliJActivityFactory(project),
            taskNotifier, dialogs);
    if (dialogs.confirmStart(tutorialViewModel)) {
      var tutorial = tutorialViewModel.getTutorial();
      if (tutorial.isDownloadDependencies()) {
        tutorial.downloadDependencies(courseViewModel.getModel(), project, taskNotifier);
      } else if (tutorial.dependenciesMissing(project, taskNotifier)) {
        return;
      }
      mainViewModelProvider.getMainViewModel(project).tutorialViewModel.set(tutorialViewModel);
      tutorial.tutorialCompleted
          .addListener(mainViewModel, mainVm -> onTutorialComplete(mainVm, courseViewModel.getModel()));
      tutorialViewModel.startNextTask();
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    MainViewModel mainViewModel = mainViewModelProvider.getMainViewModel(e.getProject());
    CourseViewModel courseViewModel = mainViewModel.courseViewModel.get();
    Authentication authentication = authenticationProvider.getAuthentication(e.getProject());
    ExercisesTreeViewModel exercisesViewModel = mainViewModel.exercisesViewModel.get();
    var selection = exercisesViewModel == null ? null
        : (ExercisesTreeViewModel.ExerciseTreeSelection) exercisesViewModel.findSelected();
    boolean isTutorialSelected =
            exercisesViewModel != null
            && authentication != null && courseViewModel != null
            && exercisesViewModel.getSelectedItem() != null
            && !(exercisesViewModel.getSelectedItem() instanceof ExerciseGroupViewModel)
            && selection.getExercise() != null
            && ExerciseViewModel.Status.TUTORIAL.equals(selection.getExercise().getStatus());

    e.getPresentation().setVisible(e.getProject() != null && isTutorialSelected);
  }

  @FunctionalInterface
  public interface TutorialAuthenticationProvider {
    Authentication getAuthentication(@Nullable Project project);
  }

  private static class DefaultDialogs implements TutorialDialogs {

    @Override
    public boolean confirmStart(@NotNull TutorialViewModel tutorialViewModel) {
      return JOptionPane.showConfirmDialog(null,
        getText("ui.tutorial.TutorialAction.confirmStart"),
        tutorialViewModel.getTitle(),
        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
    }

    @Override
    public boolean confirmCancel(@NotNull TutorialViewModel tutorialViewModel) {
      return JOptionPane.showConfirmDialog(null,
        getText("ui.tutorial.TutorialAction.confirmCancel"),
        tutorialViewModel.getTitle(),
        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
    }

    @Override
    public void end(@NotNull TutorialViewModel tutorialViewModel) {
      JOptionPane.showMessageDialog(null,
          getText("ui.tutorial.TutorialAction.end"),
          tutorialViewModel.getTitle(),
          JOptionPane.INFORMATION_MESSAGE);
    }
  }

  private void onTutorialComplete(@NotNull MainViewModel mainViewModel,
                                  @NotNull Course course) {
    TutorialViewModel viewModel = mainViewModel.tutorialViewModel.get();
    if (viewModel != null) {
      var tutorial = viewModel.getTutorial();
      if (tutorial.isDeleteDependencies()) {
        tutorial.deleteDependencies(course);
      }
      tutorial.tutorialCompleted.removeCallback(mainViewModel);
      mainViewModel.tutorialViewModel.set(null);
      // Update the progress tracker.
      Optional.ofNullable(ComponentDatabase.getNavBarToolBar()).ifPresent(tb -> tb.updateActionsImmediately(true));
      dialogs.end(viewModel);
    }
  }
}
