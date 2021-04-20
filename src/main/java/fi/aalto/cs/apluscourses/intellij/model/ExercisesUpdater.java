package fi.aalto.cs.apluscourses.intellij.model;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.async.RepeatedTask;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class ExercisesUpdater extends RepeatedTask {

  private final CourseProject courseProject;

  private final Event eventToTrigger;

  private final Notifier notifier;

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
    var progressViewModel =
        PluginSettings.getInstance().getMainViewModel(courseProject.getProject()).progressViewModel;
    progressViewModel.start(2, getText("ui.ProgressBarView.refreshingAssignments"));
    try {
      var points = dataSource.getPoints(course, authentication);
      progressViewModel.increment();
      if (Thread.interrupted()) {
        progressViewModel.increment();
        return;
      }
      points.setSubmittableExercises(course.getExerciseModules().keySet()); // TODO: remove
      var exerciseGroups = dataSource.getExerciseGroups(course, points, authentication);
      progressViewModel.increment();
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
      notifier.notify(new NetworkErrorNotification(e), courseProject.getProject());
    }
  }

}
