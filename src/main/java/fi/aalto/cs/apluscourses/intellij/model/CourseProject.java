package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import fi.aalto.cs.apluscourses.dal.TokenAuthentication;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ExercisesLazyLoader;
import fi.aalto.cs.apluscourses.model.ExercisesTree;
import fi.aalto.cs.apluscourses.model.NewsTree;
import fi.aalto.cs.apluscourses.model.Student;
import fi.aalto.cs.apluscourses.model.User;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.async.RepeatedTask;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code CourseProject} instance contains a {@link Course} and {@link Project}. In addition, it
 * contains a {@link CourseUpdater} that regularly updates the course, and an {@link Event} that is
 * triggered when an update occurs.
 */
public class CourseProject implements ExercisesLazyLoader {

  @NotNull
  private final Notifier notifier;

  @NotNull
  private final Course course;

  private volatile ExercisesTree exercisesTree;

  private volatile NewsTree newsTree;

  private final AtomicBoolean hasTriedToReadAuthenticationFromStorage = new AtomicBoolean(false);

  @NotNull
  private final RepeatedTask courseUpdater;

  @NotNull
  private final RepeatedTask exercisesUpdater;

  @NotNull
  private final Project project;

  @NotNull
  public final Event courseUpdated = new Event();

  @NotNull
  public final Event exercisesUpdated = new Event();

  @NotNull
  public final ObservableProperty<User> user = new ObservableReadWriteProperty<>(null);

  @Nullable
  private volatile Student selectedStudent = null;

  @NotNull
  private final Set<Long> lazyLoadedGroups = Collections.synchronizedSet(new HashSet<>());

  /**
   * Construct a course project from the given course, course configuration URL (used for updating),
   * and project.
   */
  public CourseProject(@NotNull Course course,
                       @NotNull URL courseUrl,
                       @NotNull Project project,
                       @NotNull Notifier notifier) {
    this.notifier = notifier;
    this.course = course;
    this.project = project;
    this.courseUpdater = new CourseUpdater(this, course, project, courseUrl, courseUpdated);
    this.exercisesUpdater = new ExercisesUpdater(this, exercisesUpdated);
  }

  /**
   * Construct a course project from the given course, exercises and course updaters,
   * and project.
   */
  public CourseProject(@NotNull Course course,
                       @NotNull RepeatedTask courseUpdater,
                       @NotNull RepeatedTask exercisesUpdater,
                       @NotNull Project project,
                       @NotNull Notifier notifier) {
    this.notifier = notifier;
    this.course = course;
    this.project = project;
    this.courseUpdater = courseUpdater;
    this.exercisesUpdater = exercisesUpdater;
  }

  /**
   * Disposes the course project, which includes stopping background updaters and clearing the
   * authentication. This should be called when a course project is no longer used.
   */
  public void dispose() {
    var myUser = this.user.get();
    if (myUser != null) {
      myUser.getAuthentication().clear();
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
    if (hasTriedToReadAuthenticationFromStorage.getAndSet(true) || user.get() != null) {
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
    return Optional.ofNullable(user.get()).map(User::getUserName).orElse("");
  }

  @NotNull
  public String getUserStudentId() {
    return Optional.ofNullable(user.get()).map(User::getStudentId).orElse("");
  }

  public int getUserAPlusId() {
    return Optional.ofNullable(user.get()).map(User::getId).orElse(0);
  }

  @Nullable
  public User getUser() {
    return user.get();
  }

  @NotNull
  public Course getCourse() {
    return course;
  }

  @Nullable
  public ExercisesTree getExerciseTree() {
    return exercisesTree;
  }

  public void setExerciseTree(@NotNull ExercisesTree exercisesTree) {
    this.exercisesTree = exercisesTree;
  }

  @Nullable
  public NewsTree getNewsTree() {
    return newsTree;
  }

  public void setNewsTree(@NotNull NewsTree newsTree) {
    this.newsTree = newsTree;
  }

  @Nullable
  public Authentication getAuthentication() {
    return user.get() == null ? null : Objects.requireNonNull(user.get()).getAuthentication();
  }

  /**
   * Sets the authentication. Any existing authentication is cleared.
   */
  public void setAuthentication(Authentication authentication) {
    try {
      var newUser = authentication == null
          ? null : course.getExerciseDataSource().getUser(authentication);
      var oldUser = this.user.getAndSet(newUser);
      if (oldUser != null) {
        oldUser.getAuthentication().clear();
      }
    } catch (IOException e) {
      notifier.notify(new NetworkErrorNotification(e), project);
    }
  }

  @NotNull
  public Project getProject() {
    return project;
  }

  @NotNull
  public RepeatedTask getCourseUpdater() {
    return courseUpdater;
  }

  @NotNull
  public RepeatedTask getExercisesUpdater() {
    return exercisesUpdater;
  }

  @Nullable
  public Student getSelectedStudent() {
    return selectedStudent;
  }

  public void setSelectedStudent(@Nullable Student selectedStudent) {
    this.selectedStudent = selectedStudent;
    lazyLoadedGroups.clear();
  }

  @Override
  public void setLazyLoadedGroup(long id) {
    if (lazyLoadedGroups.add(id)) {
      getExercisesUpdater().restart();
    }
  }

  @Override
  public boolean isLazyLoadedGroup(long id) {
    return lazyLoadedGroups.contains(id);
  }
}
