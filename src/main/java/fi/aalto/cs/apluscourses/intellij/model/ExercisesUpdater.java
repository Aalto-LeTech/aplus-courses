package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
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

  private final Project project;

  /**
   * Construct an updater with the given parameters.
   */
  public ExercisesUpdater(@NotNull CourseProject courseProject,
                          @NotNull Event eventToTrigger,
                          @NotNull Notifier notifier,
                          @NotNull Project project,
                          long updateInterval) {
    super(updateInterval);
    this.courseProject = courseProject;
    this.eventToTrigger = eventToTrigger;
    this.notifier = notifier;
    this.project = project;
  }

  public ExercisesUpdater(@NotNull CourseProject courseProject,
                          @NotNull Event eventToTrigger,
                          @NotNull Project project) {
    this(courseProject, eventToTrigger, new DefaultNotifier(), project,
        PluginSettings.UPDATE_INTERVAL);
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
        PluginSettings.getInstance().getMainViewModel(project).progressViewModel;
    progressViewModel.start(2, "Refreshing assignments...");
    try {
      var points = dataSource.getPoints(course, authentication);
      progressViewModel.increment();
      if (Thread.interrupted()) {
        progressViewModel.stop();
        return;
      }
      points.setSubmittableExercises(course.getExerciseModules().keySet()); // TODO: remove
      var exerciseGroups = dataSource.getExerciseGroups(course, points, authentication);
      progressViewModel.increment();
      if (Thread.interrupted()) {
        progressViewModel.stop();
        return;
      }
      courseProject.setExerciseGroups(exerciseGroups);
      eventToTrigger.trigger();
    } catch (IOException e) {
      notifier.notify(new NetworkErrorNotification(e), courseProject.getProject());
    } finally {
      progressViewModel.stop();
    }
  }

}
