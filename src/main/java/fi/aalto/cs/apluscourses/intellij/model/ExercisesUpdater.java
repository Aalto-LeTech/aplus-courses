package fi.aalto.cs.apluscourses.intellij.model;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.Points;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.async.RepeatedTask;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
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
      if (courseProject.getExerciseGroups() != null) {
        courseProject.setExerciseGroups(Collections.emptyList());
        eventToTrigger.trigger();
      }
      return;
    }
    var progressViewModel =
        PluginSettings.getInstance().getMainViewModel(courseProject.getProject()).progressViewModel;
    var progress =
            progressViewModel.start(2, getText("ui.ProgressBarView.refreshingAssignments"), false);
    try {
      var points = dataSource.getPoints(course, authentication);
      progress.increment();
      if (Thread.interrupted()) {
        progress.finish();
        return;
      }
      points.setSubmittableExercises(course.getExerciseModules().keySet()); // TODO: remove
      var exerciseGroups
          = dataSource.getExerciseGroups(course, points, course.getTutorials(), authentication);
      if (courseProject.getExerciseGroups() == null) {
        courseProject.setExerciseGroups(exerciseGroups);
        eventToTrigger.trigger();
      }
      for (var exerciseGroup : exerciseGroups) {
        for (var exercise : exerciseGroup.getExercises().values()) {
          if (Thread.interrupted()) {
            progress.finish();
            return;
          }
          addSubmissionResults(course, exercise, points, authentication);
        }
      }
      progress.finish();
      if (Thread.interrupted()) {
        return;
      }
      courseProject.setExerciseGroups(exerciseGroups);
      eventToTrigger.trigger();
    } catch (IOException e) {
      var observable = PluginSettings
          .getInstance()
          .getMainViewModel(courseProject.getProject())
          .exercisesViewModel;
      var exercisesViewModel = observable.get();
      if (exercisesViewModel != null) {
        exercisesViewModel.setAuthenticated(false);
        observable.valueChanged();
      }
      progress.finish();
      notifier.notify(new NetworkErrorNotification(e), courseProject.getProject());
    }
  }

  private void addSubmissionResults(@NotNull Course course,
                                    @NotNull Exercise exercise,
                                    @NotNull Points points,
                                    @NotNull Authentication authentication)
      throws IOException {
    var dataSource = course.getExerciseDataSource();
    var baseUrl = course.getApiUrl() + "submissions/";
    var submissionIds = points.getSubmissions()
        .getOrDefault(exercise.getId(), Collections.emptyList());
    for (var id : submissionIds) {
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
      if (Thread.interrupted()) {
        return;
      }
    }
  }

}
