package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import fi.aalto.cs.apluscourses.dal.TokenAuthentication;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.utils.Event;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code CourseProject} instance contains a {@link Course} and {@link Project}. In addition, it
 * contains a {@link CourseUpdater} that regularly updates the course, and an {@link Event} that is
 * triggered when an update occurs.
 */
public class CourseProject {

  @NotNull
  private final Course course;

  @NotNull
  private volatile List<ExerciseGroup> exerciseGroups;

  private final AtomicBoolean hasTriedToReadAuthenticationFromStorage = new AtomicBoolean(false);

  private volatile Authentication authentication = null;

  @NotNull
  private final CourseUpdater courseUpdater;

  @NotNull
  private final ExercisesUpdater exercisesUpdater;

  @NotNull
  private final Project project;

  @NotNull
  public final Event courseUpdated;

  @NotNull
  public final Event exercisesUpdated;

  /**
   * Construct a course project from the given course, course configuration URL (used for updating),
   * and project.
   */
  public CourseProject(@NotNull Course course, @NotNull URL courseUrl, @NotNull Project project) {
    this.course = course;
    this.project = project;
    this.courseUpdated = new Event();
    this.exercisesUpdated = new Event();
    this.courseUpdater = new CourseUpdater(course, project, courseUrl, courseUpdated);
    this.exercisesUpdater = new ExercisesUpdater(this, exercisesUpdated, project);
  }

  /**
   * Disposes the course project, which includes stopping background updaters and clearing the
   * authentication. This should be called when a course project is no longer used.
   */
  public void dispose() {
    if (authentication != null) {
      authentication.clear();
    }
    courseUpdater.stop();
    exercisesUpdater.stop();
  }

  /**
   * <p>Sets authentication to the one that is read from the password storage and constructed with
   * the given factory.</p>
   *
   * <p>This method does anything only when it's called the first time for an instance. All the
   * subsequent calls do nothing.</p>
   *
   * @param passwordStorage Password storage.
   * @param factory         Authentication factory.
   */
  public void readAuthenticationFromStorage(@Nullable PasswordStorage passwordStorage,
                                            @NotNull TokenAuthentication.Factory factory) {
    if (hasTriedToReadAuthenticationFromStorage.getAndSet(true) || authentication != null) {
      return;
    }
    Optional.ofNullable(passwordStorage)
        .map(PasswordStorage::restorePassword)
        .map(factory::create)
        .ifPresent(this::setAuthentication);
  }

  @NotNull
  public Course getCourse() {
    return course;
  }

  @NotNull
  public List<ExerciseGroup> getExerciseGroups() {
    return exerciseGroups;
  }

  public void setExerciseGroups(@NotNull List<ExerciseGroup> exerciseGroups) {
    this.exerciseGroups = exerciseGroups;
  }

  @Nullable
  public Authentication getAuthentication() {
    return authentication;
  }

  /**
   * Sets the authentication. Any existing authentication is cleared.
   */
  public void setAuthentication(@NotNull Authentication authentication) {
    if (this.authentication != null) {
      this.authentication.clear();
    }
    this.authentication = authentication;
  }

  @NotNull
  public Project getProject() {
    return project;
  }

  @NotNull
  public CourseUpdater getCourseUpdater() {
    return courseUpdater;
  }

  @NotNull
  public ExercisesUpdater getExercisesUpdater() {
    return exercisesUpdater;
  }

}
