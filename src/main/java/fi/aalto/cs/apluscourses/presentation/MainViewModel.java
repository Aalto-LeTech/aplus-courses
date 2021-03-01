package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import fi.aalto.cs.apluscourses.dal.TokenAuthentication;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.model.InvalidAuthenticationException;
import fi.aalto.cs.apluscourses.model.Points;
import fi.aalto.cs.apluscourses.presentation.exercise.EmptyExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainViewModel {

  public static final Logger logger = LoggerFactory.getLogger(MainViewModel.class);

  public final Event disposing = new Event();

  @NotNull
  public final ObservableProperty<CourseViewModel> courseViewModel =
      new ObservableReadWriteProperty<>(null);

  @NotNull
  public final ObservableProperty<ExercisesTreeViewModel> exercisesViewModel =
      new ObservableReadWriteProperty<>(null);

  @NotNull
  public final ObservableProperty<Authentication> authentication =
      new ObservableReadWriteProperty<>(null);

  @NotNull
  private final Options exerciseFilterOptions;

  private final AtomicBoolean hasTriedToReadAuthenticationFromStorage = new AtomicBoolean(false);

  private Map<Long, Exercise> inGrading = new ConcurrentHashMap<>();

  /**
   * Instantiates a class representing the whole main view of the plugin.
   */
  public MainViewModel(@NotNull Options exerciseFilterOptions) {
    this.exerciseFilterOptions = exerciseFilterOptions;
    courseViewModel.addValueObserver(this, MainViewModel::updateExercises);
    authentication.addValueObserver(this, MainViewModel::updateExercises);
  }

  private void updateExercises() {
    Course course = null;
    var localCourseViewModel = courseViewModel.get();
    if (localCourseViewModel != null) {
      course = localCourseViewModel.getModel();
    }
    Authentication auth = authentication.get();
    if (course == null || auth == null) {
      ExercisesTreeViewModel emptyModel = new ExercisesTreeViewModel(new ArrayList<>(),
              new Options());
      if (course == null) {
        emptyModel = new EmptyExercisesTreeViewModel(new ArrayList<>(), new Options());
      }
      emptyModel.setAuthenticated(auth != null);
      exercisesViewModel.set(emptyModel);
      exercisesViewModel.get().setProjectReady(hasTriedToReadAuthenticationFromStorage.get());
      return;
    }
    ExerciseDataSource dataSource = course.getExerciseDataSource();
    try {
      Points points = dataSource.getPoints(course, auth);
      points.setSubmittableExercises(course.getExerciseModules().keySet()); // TODO: remove
      List<ExerciseGroup> exerciseGroups = dataSource.getExerciseGroups(course, points, auth);
      inGrading.forEach((id, exercise) -> setInGrading(exerciseGroups, id));
      exercisesViewModel.set(new ExercisesTreeViewModel(exerciseGroups, exerciseFilterOptions));
      exercisesViewModel.get().setAuthenticated(auth != null);
      exercisesViewModel.get().setProjectReady(hasTriedToReadAuthenticationFromStorage.get());
    } catch (InvalidAuthenticationException e) {
      logger.error("Failed to fetch exercises due to authentication issues", e);
      // TODO: might want to communicate this to the user somehow
    } catch (IOException e) {
      logger.error("Failed to fetch exercises", e);
      // This too
    }
  }

  public void dispose() {
    disposing.trigger();
  }

  public void setAuthentication(Authentication auth) {
    disposing.addListener(auth, Authentication::clear);
    Optional.ofNullable(authentication.getAndSet(auth)).ifPresent(Authentication::clear);
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
    if (hasTriedToReadAuthenticationFromStorage.getAndSet(true) || authentication.get() != null) {
      exercisesViewModel.get().setProjectReady(true);
      return;
    }
    Optional.ofNullable(passwordStorage)
        .map(PasswordStorage::restorePassword)
        .map(factory::create)
        .ifPresent(this::setAuthentication);
    exercisesViewModel.get().setProjectReady(true);
    if (!exercisesViewModel.get().isAuthenticated()) {
      this.updateExercises();
    }
  }

  @Nullable
  public ExercisesTreeViewModel getExercises() {
    return exercisesViewModel.get();
  }

  @NotNull
  public Options getExerciseFilterOptions() {
    return exerciseFilterOptions;
  }

  private static void setInGrading(List<ExerciseGroup> exerciseGroups, long id) {
    exerciseGroups
        .stream()
        .map(ExerciseGroup::getExercises)
        .filter(exercise -> exercise.containsKey(id))
        .forEach(exercise -> exercise.get(id).setInGrading(true));
  }

  /**
   * Modifies the 'inGrading' status of the exercise in 'exercisesViewModel' which corresponds to
   * the given exercise (i.e. has the same ID). The given exercise is not modified. The observers of
   * 'exercisesViewModel' are notified.
   */
  public void setSubmittedForGrading(@NotNull Exercise submittedForGrading) {
    Exercise previous = inGrading.putIfAbsent(submittedForGrading.getId(), submittedForGrading);
    if (previous == null) {
      // This method gets called repeatedly by SubmissionStatusUpdater, so we check whether the
      // entry was already there or not, and only update the tree if it wasn't there previously.
      setInGrading(exercisesViewModel.get().getModel(), submittedForGrading.getId());
      exercisesViewModel.valueChanged();
    }
  }

  /**
   * Marks the exercise with the ID of the given exercise as being graded. 'setSubmittedForGrading'
   * must have been called with the same exercise ID previously. The given exercise is not modified.
   */
  public void setGradingDone(@NotNull Exercise submittedForGrading) {
    // We don't call exercisesViewModel.valueChanged() to update the tree here.
    // Once grading is done, the tree is updated anyways by SubmissionStatusUpdater.
    inGrading.remove(submittedForGrading.getId());
  }

  /**
   * Triggers updateExercises when the project is not A+.
   */
  public void setProjectReady() {
    this.updateExercises();
  }
}
