package fi.aalto.cs.apluscourses.intellij.model;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.model.Points;
import fi.aalto.cs.apluscourses.model.Progress;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.presentation.ProgressViewModel;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.async.RepeatedTask;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;

public class ExercisesUpdater extends RepeatedTask {

  private final CourseProject courseProject;

  private final Event eventToTrigger;

  private final Notifier notifier;

  private final Set<Long> submissionsInGrading = ConcurrentHashMap.newKeySet();

  /**
   * Construct an updater with the given parameters.
   */
  public ExercisesUpdater(@NotNull CourseProject courseProject,
                          @NotNull Event eventToTrigger,
                          @NotNull Notifier notifier,
                          long updateInterval) {
    super(updateInterval);
    this.courseProject = courseProject;
    this.eventToTrigger = eventToTrigger;
    this.notifier = notifier;
  }

  public ExercisesUpdater(@NotNull CourseProject courseProject,
                          @NotNull Event eventToTrigger) {
    this(courseProject, eventToTrigger, new DefaultNotifier(), PluginSettings.UPDATE_INTERVAL);
  }

  @Override
  protected void doTask() {
    var course = courseProject.getCourse();
    var dataSource = course.getExerciseDataSource();
    var authentication = courseProject.getAuthentication();
    if (authentication == null) {
      return;
    }
    var progressViewModel = PluginSettings.getInstance()
            .getMainViewModel(courseProject.getProject()).progressViewModel;
    var progress =
            progressViewModel.start(3, getText("ui.ProgressBarView.refreshingAssignments"), false);
    try {
      var points = dataSource.getPoints(course, authentication);
      progressViewModel.increment(progress);
      if (Thread.interrupted()) {
        progressViewModel.stop(progress);
        return;
      }
      var exerciseGroups = dataSource.getExerciseGroups(course, points, authentication);
      progressViewModel.increment(progress);
      if (courseProject.getExerciseGroups() == null) {
        courseProject.setExerciseGroups(exerciseGroups);
        eventToTrigger.trigger();
      }

      prefetchExercises(exerciseGroups, points, authentication);

      addExercises(exerciseGroups, points, authentication, progressViewModel, progress);
      progressViewModel.increment(progress);
      for (var exerciseGroup : exerciseGroups) {
        for (var exercise : exerciseGroup.getExercises()) {
          if (Thread.interrupted()) {
            progressViewModel.stop(progress);
            return;
          }
          addSubmissionResults(exercise, points, authentication);
        }
      }
      progressViewModel.stop(progress);
      if (Thread.interrupted()) {
        return;
      }
      courseProject.setExerciseGroups(exerciseGroups);
      eventToTrigger.trigger();
    } catch (IOException e) {
      progressViewModel.stop(progress);
      var observable = PluginSettings
          .getInstance()
          .getMainViewModel(courseProject.getProject())
          .exercisesViewModel;
      var exercisesViewModel = observable.get();
      if (exercisesViewModel != null) {
        exercisesViewModel.setAuthenticated(false);
        observable.valueChanged();
      }
      notifier.notify(new NetworkErrorNotification(e), courseProject.getProject());
    }
  }

  private void prefetchExercises(@NotNull List<ExerciseGroup> exerciseGroups,
                                 @NotNull Points points,
                                 @NotNull Authentication authentication) {
    var dataSource = courseProject.getCourse().getExerciseDataSource();

    for (var exerciseGroup : exerciseGroups) {
      for (var exerciseId : points.getExercises(exerciseGroup.getId())) {
        dataSource.prefetchExercise(exerciseId, authentication);
      }
    }
  }

  private void addExercises(@NotNull List<ExerciseGroup> exerciseGroups,
                            @NotNull Points points,
                            @NotNull Authentication authentication,
                            @NotNull ProgressViewModel progressViewModel,
                            @NotNull Progress progress) throws IOException {
    var dataSource = courseProject.getCourse().getExerciseDataSource();
    progressViewModel.incrementMaxValue(progress, points.getExercisesAmount());
    for (var exerciseGroup : exerciseGroups) {
      for (var exerciseId : points.getExercises(exerciseGroup.getId())) {
        if (Thread.interrupted()) {
          return;
        }
        var exercise = dataSource
            .getExercise(exerciseId, points, authentication, ZonedDateTime.now().minusDays(7));
        exerciseGroup.addExercise(exercise);
        progressViewModel.increment(progress);
      }
      courseProject.setExerciseGroups(exerciseGroups);
      eventToTrigger.trigger();
    }
  }

  private void addSubmissionResults(@NotNull Exercise exercise,
                                    @NotNull Points points,
                                    @NotNull Authentication authentication)
      throws IOException {
    var dataSource = courseProject.getCourse().getExerciseDataSource();
    var baseUrl = courseProject.getCourse().getApiUrl() + "submissions/";
    var submissionIds = points.getSubmissions(exercise.getId());
    for (var id : submissionIds) {
      if (Thread.interrupted()) {
        return;
      }
      // Ignore cache for submissions that had the status WAITING
      var cacheTime = submissionsInGrading.remove(id)
          ? OffsetDateTime.MAX.toZonedDateTime()
          : ZonedDateTime.now().minusDays(7);
      var submission = dataSource.getSubmissionResult(
          baseUrl + id + "/", exercise, authentication, cacheTime);
      if (submission.getStatus() == SubmissionResult.Status.WAITING) {
        submissionsInGrading.add(id);
      }
      exercise.addSubmissionResult(submission);
    }
  }

}
