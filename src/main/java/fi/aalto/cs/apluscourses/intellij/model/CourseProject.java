package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import fi.aalto.cs.apluscourses.dal.TokenAuthentication;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.model.User;
import fi.aalto.cs.apluscourses.utils.Event;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code CourseProject} instance contains a {@link Course} and {@link Project}. In addition, it
 * contains a {@link CourseUpdater} that regularly updates the course, and an {@link Event} that is
 * triggered when an update occurs.
 */
public class CourseProject {

  private static final Logger logger = LoggerFactory.getLogger(CourseProject.class);

  @NotNull
  private final Course course;

  private volatile List<ExerciseGroup> exerciseGroups;

  private final AtomicBoolean hasTriedToReadAuthenticationFromStorage = new AtomicBoolean(false);

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
  
  private volatile User user;

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
    this.exercisesUpdater = new ExercisesUpdater(this, exercisesUpdated);
  }

  /**
   * Disposes the course project, which includes stopping background updaters and clearing the
   * authentication. This should be called when a course project is no longer used.
   */
  public void dispose() {
    if (user != null) {
      user.getAuthentication().clear();
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
    if (hasTriedToReadAuthenticationFromStorage.getAndSet(true) || user != null) {
      return;
    }
    Optional.ofNullable(passwordStorage)
        .map(PasswordStorage::restorePassword)
        .map(factory::create)
        .ifPresent(this::setAuthentication);
  }

  /**
   * Removes user from password storage.
   */
  public void removePasswordFromStorage(@NotNull PasswordStorage.Factory passwordStorageFactory) {
    var passwordStorage = passwordStorageFactory.create(course.getApiUrl());
    if (passwordStorage != null) {
      passwordStorage.remove();
    }
  }

  @NotNull
  public String getUserName() {
    return user == null ? "" : user.getUserName();
  }

  @NotNull
  public Course getCourse() {
    return course;
  }

  @Nullable
  public List<ExerciseGroup> getExerciseGroups() {
    return exerciseGroups;
  }

  public void setExerciseGroups(@NotNull List<ExerciseGroup> exerciseGroups) {
    this.exerciseGroups = exerciseGroups;
  }

  @Nullable
  public Authentication getAuthentication() {
    return user == null ? null : user.getAuthentication();
  }

  /**
   * Sets the authentication. Any existing authentication is cleared.
   */
  public void setAuthentication(Authentication authentication) {
    if (this.user != null) {
      this.user.getAuthentication().clear();
    }
    if (authentication == null) {
      this.user = null;
    } else {
      try {
        this.user = course.getExerciseDataSource().getUser(authentication);
      } catch (IOException e) {
        logger.error("Failed to fetch user data", e);
      }
    }
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
