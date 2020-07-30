package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.dal.APlusExerciseDataSource;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.model.InvalidAuthenticationException;
import fi.aalto.cs.apluscourses.model.Points;
import fi.aalto.cs.apluscourses.presentation.base.BaseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadOnlyProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.io.IOException;
import java.util.List;

import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class MainViewModel {

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

  /**
   * Instantiates a new view model for the main object.
   */
  public MainViewModel() {
    courseViewModel.addValueObserver(this, MainViewModel::updateExercises);
    authentication.addValueObserver(this, MainViewModel::updateExercises);
  }

  private void updateExercises() {
    Course course = Optional.ofNullable(courseViewModel.get())
        .map(BaseViewModel::getModel)
        .orElse(null);
    Authentication auth = authentication.get();
    if (course == null || auth == null) {
      exercisesViewModel.set(null);
      return;
    }
    ExerciseDataSource dataSource = course.getExerciseDataSource();
    try {
      Points points = dataSource.getPoints(course, auth);
      List<ExerciseGroup> exerciseGroups = dataSource.getExerciseGroups(course, points, auth);
      exercisesViewModel.set(new ExercisesTreeViewModel(exerciseGroups));
    } catch (InvalidAuthenticationException e) {
      // TODO: might want to communicate this to the user somehow
    } catch (IOException e) {
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
}
