package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.utils.Event;
import java.net.URL;
import org.jetbrains.annotations.NotNull;

public class CourseProject {

  @NotNull
  private final Course course;

  @NotNull
  private final CourseUpdater courseUpdater;

  @NotNull
  public final Event courseUpdated;

  /**
   * Construct a course project from the given course, course configuration URL (used for updating),
   * and project.
   */
  public CourseProject(@NotNull Course course, @NotNull URL courseUrl, @NotNull Project project) {
    this.course = course;
    this.courseUpdated = new Event();
    this.courseUpdater = new CourseUpdater(
        course, project, courseUrl, courseUpdated, new DefaultNotifier(),
        PluginSettings.COURSE_UPDATE_INTERVAL);
  }

  @NotNull
  public Course getCourse() {
    return course;
  }

  @NotNull
  public CourseUpdater getCourseUpdater() {
    return courseUpdater;
  }

}
