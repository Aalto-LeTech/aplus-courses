package fi.aalto.cs.apluscourses.intellij.model;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.DummySubmissionResult;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.model.ExercisesTree;
import fi.aalto.cs.apluscourses.model.Points;
import fi.aalto.cs.apluscourses.model.Progress;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.utils.APlusLogger;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.async.RepeatedTask;
import fi.aalto.cs.apluscourses.utils.cache.CachePreferences;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class ExercisesUpdater extends RepeatedTask {

  private static final Logger logger = APlusLogger.logger;

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
    logger.debug("Starting exercises update");
    var course = courseProject.getCourse();
    var dataSource = course.getExerciseDataSource();
    var authentication = courseProject.getAuthentication();
    if (authentication == null) {
      if (courseProject.getExerciseTree() != null) {
        courseProject.setExerciseTree(new ExercisesTree());
        eventToTrigger.trigger();
      }
      logger.warn("Not authenticated");
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
      logger.info("Exercise groups count: {}", exerciseGroups.size());
      if (Thread.interrupted()) {
        progress.finish();
        return;
      }
      progress.increment();
      var selectedStudent = courseProject.getSelectedStudent();
      logger.info("Selected student: {}", selectedStudent);
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

      progress.incrementMaxValue(submissionsToBeLoadedCount(exerciseGroups, points)
          + exercisesToBeLoadedCount(exerciseGroups));
      addExercises(exerciseGroups, points, authentication, progress);

      progress.finish();
      if (Thread.interrupted()) {
        return;
      }

      eventToTrigger.trigger();
      logger.debug("Exercises update done");
    } catch (IOException e) {
      var mainVm = PluginSettings
          .getInstance()
          .getMainViewModel(courseProject.getProject());
      var cardVm = mainVm.toolWindowCardViewModel;
      cardVm.setAuthenticated(false);
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
    courseProject.setExerciseTree(exercisesTree);
    for (var exerciseGroup : exerciseGroups) {

      if (courseProject.isLazyLoadedGroup(exerciseGroup.getId())) {
        for (var exerciseId : points.getExercises(exerciseGroup.getId())) {
          if (Thread.interrupted()) {
            return;
          }
          var exercise = dataSource.getExercise(exerciseId, points, course.getOptionalCategories(),
              course.getTutorials(), authentication, CachePreferences.GET_MAX_ONE_WEEK_OLD);
          exerciseGroup.addExercise(exercise);

          progress.increment();

          addSubmissionResults(exercise, points, authentication, progress);

          eventToTrigger.trigger();
        }

      }
    }
  }

  private void addDummySubmissionResults(@NotNull List<ExerciseGroup> exerciseGroups,
                                         @NotNull Points points) {
    for (var exerciseGroup : exerciseGroups) {
      if (Thread.interrupted()) {
        return;
      }
      for (var exercise : exerciseGroup.getExercises()) {
        var submissionIds = points.getSubmissions(exercise.getId());
        for (var id : submissionIds) {
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

      // Ignore cache for submissions that had the status WAITING
      var cachePreference = submissionsInGrading.remove(id)
          ? CachePreferences.GET_NEW_AND_KEEP
          : CachePreferences.GET_MAX_ONE_WEEK_OLD;
      var submission = dataSource.getSubmissionResult(
          baseUrl + id + "/", exercise, authentication, cachePreference);
      if (submission.getStatus() == SubmissionResult.Status.WAITING) {
        submissionsInGrading.add(id);
      }

      exercise.addSubmissionResult(submission);
      progress.increment();
    }
  }

  private int submissionsToBeLoadedCount(@NotNull List<ExerciseGroup> exerciseGroups, @NotNull Points points) {
    return exerciseGroups.stream()
        .filter(group -> courseProject.isLazyLoadedGroup(group.getId()))
        .mapToInt(group -> group.getExercises().stream()
            .mapToInt(exercise -> points.getSubmissionsAmount(exercise.getId())).sum())
        .sum();
  }

  private int exercisesToBeLoadedCount(@NotNull List<ExerciseGroup> exerciseGroups) {
    return exerciseGroups.stream()
        .filter(group -> courseProject.isLazyLoadedGroup(group.getId()))
        .mapToInt(group -> group.getExercises().size())
        .sum();
  }

}
