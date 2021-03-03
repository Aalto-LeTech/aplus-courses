package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.utils.Event;
import java.net.URL;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code CourseProject} instance contains a {@link Course} and {@link Project}. In addition, it
 * contains a {@link CourseUpdater} that regularly updates the course, and an {@link Event} that is
 * triggered when an update occurs.
 */
public class CourseProject {

  @NotNull
  private final Course course;

  @NotNull
  private final CourseUpdater courseUpdater;

  @NotNull
  private final Project project;

  @NotNull
  public final Event courseUpdated;

  /**
   * Construct a course project from the given course, course configuration URL (used for updating),
   * and project.
   */
  public CourseProject(@NotNull Course course, @NotNull URL courseUrl, @NotNull Project project) {
    this.course = course;
    this.project = project;
    this.courseUpdated = new Event();
    this.courseUpdater = new CourseUpdater(course, project, courseUrl, courseUpdated);
  }

  @NotNull
  public Course getCourse() {
    return course;
  }

  @NotNull
  public Project getProject() {
    return project;
  }

  @NotNull
  public CourseUpdater getCourseUpdater() {
    return courseUpdater;
  }

}
