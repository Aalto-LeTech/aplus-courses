package fi.aalto.cs.apluscourses.intellij.model;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.DummyExercise;
import fi.aalto.cs.apluscourses.model.DummySubmissionResult;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.model.ExercisesTree;
import fi.aalto.cs.apluscourses.model.Points;
import fi.aalto.cs.apluscourses.model.Progress;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.async.RepeatedTask;
import fi.aalto.cs.apluscourses.utils.cache.CachePreferences;
import java.io.IOException;
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
      if (courseProject.getExerciseTree() != null) {
        courseProject.setExerciseTree(new ExercisesTree());
        eventToTrigger.trigger();
      }
      return;
    }
    var progressViewModel =
        PluginSettings.getInstance().getMainViewModel(courseProject.getProject()).progressViewModel;
    var progress =
        progressViewModel.start(3, getText("ui.ProgressBarView.refreshingAssignments"), false);
    try {
      progress.increment();
      if (Thread.interrupted()) {
        progress.finish();
        return;
      }
      var exerciseGroups = dataSource.getExerciseGroups(course, authentication);
      if (Thread.interrupted()) {
        progress.finish();
        return;
      }
      progress.increment();
      var selectedStudent = courseProject.getSelectedStudent();
      var exerciseTree = new ExercisesTree(exerciseGroups, selectedStudent);
      if (courseProject.getExerciseTree() == null) {
        courseProject.setExerciseTree(exerciseTree);
        eventToTrigger.trigger();
      }
      var points = dataSource.getPoints(course, authentication, selectedStudent);

      addDummySubmissionResults(exerciseGroups, points);
      if (Thread.interrupted()) {
        progress.finish();
        return;
      }

      progress.incrementMaxValue(submissionsAmount(exerciseGroups, points) + exercisesAmount(exerciseGroups));
      addExercises(exerciseGroups, points, authentication, progress);

      progress.finish();
      if (Thread.interrupted()) {
        return;
      }
      courseProject.setExerciseTree(exerciseTree);
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

  private void addExercises(@NotNull List<ExerciseGroup> exerciseGroups,
                            @NotNull Points points,
                            @NotNull Authentication authentication,
                            @NotNull Progress progress) throws IOException {
    var course = courseProject.getCourse();
    var dataSource = course.getExerciseDataSource();
    var selectedStudent = courseProject.getSelectedStudent();
    var exercisesTree = new ExercisesTree(exerciseGroups, selectedStudent);
    for (var exerciseGroup : exerciseGroups) {

      if (courseProject.isLazyLoaded(exerciseGroup.getId())) {
        for (var exerciseId : points.getExercises(exerciseGroup.getId())) {
          if (Thread.interrupted()) {
            return;
          }
          var exercise = dataSource.getExercise(exerciseId, points, course.getTutorials(),
              authentication, CachePreferences.GET_MAX_ONE_WEEK_OLD);
          exerciseGroup.addExercise(exercise);

          progress.increment();

          addSubmissionResults(exercise, points, authentication, progress);

          courseProject.setExerciseTree(exercisesTree);
          eventToTrigger.trigger();
        }

      }
    }
  }

  private void addDummySubmissionResults(@NotNull List<ExerciseGroup> exerciseGroups,
                                         @NotNull Points points) {
    for (var exerciseGroup : exerciseGroups) {
      for (var exercise : exerciseGroup.getExercises()) {
        var submissionIds = points.getSubmissions(exercise.getId());
        for (var id : submissionIds) {
          if (Thread.interrupted()) {
            return;
          }
          exercise.addSubmissionResult(new DummySubmissionResult(id, exercise));
        }
      }
    }
  }

  private void addSubmissionResults(@NotNull Exercise exercise,
                                    @NotNull Points points,
                                    @NotNull Authentication authentication,
                                    @NotNull Progress progress)
      throws IOException {
    var dataSource = courseProject.getCourse().getExerciseDataSource();
    var baseUrl = courseProject.getCourse().getApiUrl() + "submissions/";
    var submissionIds = points.getSubmissions(exercise.getId());
    for (var id : submissionIds) {
      if (Thread.interrupted()) {
        return;
      }
      SubmissionResult submission;
      if (exercise instanceof DummyExercise) {
        submission = new DummySubmissionResult(id, exercise);
      } else {
        // Ignore cache for submissions that had the status WAITING
        var cachePreference = submissionsInGrading.remove(id)
            ? CachePreferences.GET_NEW_AND_KEEP
            : CachePreferences.GET_MAX_ONE_WEEK_OLD;
        submission = dataSource.getSubmissionResult(
            baseUrl + id + "/", exercise, authentication, cachePreference);
        if (submission.getStatus() == SubmissionResult.Status.WAITING) {
          submissionsInGrading.add(id);
        }
      }
      exercise.addSubmissionResult(submission);
      progress.increment();
    }
  }

  private int submissionsAmount(@NotNull List<ExerciseGroup> exerciseGroups, @NotNull Points points) {
    return exerciseGroups.stream()
        .filter(group -> courseProject.isLazyLoaded(group.getId()))
        .mapToInt(group -> group.getExercises().stream()
            .mapToInt(exercise -> points.getSubmissionsAmount(exercise.getId())).sum())
        .sum();
  }

  private int exercisesAmount(@NotNull List<ExerciseGroup> exerciseGroups) {
    return exerciseGroups.stream()
        .filter(group -> courseProject.isLazyLoaded(group.getId()))
        .mapToInt(group -> group.getExercises().size())
        .sum();
  }

}
