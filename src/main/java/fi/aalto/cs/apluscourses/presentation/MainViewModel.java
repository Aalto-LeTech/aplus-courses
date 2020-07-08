package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.utils.observable.CompoundObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import org.jetbrains.annotations.NotNull;

public class MainViewModel {

  @NotNull
  public final ObservableProperty<CourseViewModel> courseViewModel
      = new ObservableReadWriteProperty<>(null);

  @NotNull
  public final ObservableProperty<APlusAuthenticationViewModel> authenticationViewModel
      = new ObservableReadWriteProperty<>(null);

  @NotNull
  public final ObservableProperty<ExercisesTreeViewModel> exercisesViewModel =
      new CompoundObservableProperty<>(
          courseViewModel,
          authenticationViewModel,
          ExercisesTreeViewModel::fromCourseAndAuthentication);

  @NotNull
  public ObservableProperty<APlusAuthenticationViewModel> getAuthenticationViewModel() {
    return authenticationViewModel;
  }

  @NotNull
  public ObservableProperty<CourseViewModel> getCourseViewModel() {
    return courseViewModel;
  }
}
