package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.model.InvalidAuthenticationException;
import fi.aalto.cs.apluscourses.model.Points;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.io.IOException;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class MainViewModel {

  public final Event disposing = new Event();

  @NotNull
  public final ObservableProperty<CourseViewModel> courseViewModel =
      new ObservableReadWriteProperty<>(null);

  @NotNull
  public final ObservableProperty<ExercisesTreeViewModel> exercisesViewModel =
      new ObservableReadWriteProperty<>(null);

  public final ObservableProperty<ExerciseDataSource> exerciseDataSource =
      new ObservableReadWriteProperty<>(null);

  /**
   * Instantiates a new view model for the main object.
   */
  public MainViewModel() {
    courseViewModel.addValueObserver(this, MainViewModel::updateExercises);
    exerciseDataSource.addValueObserver(this, MainViewModel::updateExercises);
  }

  private void updateExercises() {
    ExerciseDataSource localExerciseDataSource = exerciseDataSource.get();
    CourseViewModel course = courseViewModel.get();
    if (course == null || localExerciseDataSource == null) {
      return;
    }
    try {
      Points points
          = localExerciseDataSource.getPoints(course.getModel());
      List<ExerciseGroup> exerciseGroups
          = localExerciseDataSource.getExerciseGroups(course.getModel(), points);
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

  @NotNull
  public ObservableProperty<CourseViewModel> getCourseViewModel() {
    return courseViewModel;
  }
}
