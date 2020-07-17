package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.model.InvalidAuthenticationException;
import fi.aalto.cs.apluscourses.model.Main;
import fi.aalto.cs.apluscourses.presentation.base.BaseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class MainViewModel extends BaseViewModel<Main> {

  @NotNull
  public final ObservableProperty<CourseViewModel> courseViewModel =
      new ObservableViewModelProperty<>(null, getModel()::setCourse);

  @NotNull
  public final ObservableProperty<ExercisesTreeViewModel> exercisesViewModel =
      new ObservableViewModelProperty<>(null, getModel()::setExerciseGroups);

  @NotNull
  private final AuthenticationViewModel authenticationViewModel =
      new AuthenticationViewModel(getModel().getExerciseDataSource().getAuthentication());

  /**
   * Instantiates a new view model for the main object.
   *
   * @param model A {@link Main} object.
   */
  public MainViewModel(@NotNull Main model) {
    super(model);
    authenticationViewModel.changed.addListener(this, MainViewModel::updateExercises);
    courseViewModel.addValueObserver(this, MainViewModel::updateExercises);
  }

  @NotNull
  public AuthenticationViewModel getAuthentication() {
    return authenticationViewModel;
  }

  private void updateExercises() {
    CourseViewModel course = courseViewModel.get();
    if (course == null || !authenticationViewModel.isSet()) {
      return;
    }
    try {
      exercisesViewModel.set(new ExercisesTreeViewModel(getModel().getExerciseDataSource()
          .getExerciseGroups(course.getModel())));
    } catch (InvalidAuthenticationException e) {
      // TODO: might want to communicate this to the user somehow
    } catch (IOException e) {
      // This too
    }
  }
}
