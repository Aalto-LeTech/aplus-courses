package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import fi.aalto.cs.apluscourses.model.InvalidAuthenticationException;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MainViewModel {

  @NotNull
  public final ObservableProperty<CourseViewModel> courseViewModel =
      new ObservableReadWriteProperty<>(null);

  @NotNull
  public final ObservableProperty<ExercisesTreeViewModel> exercisesViewModel =
      new ObservableReadWriteProperty<>(null);

  private ExerciseDataSource exerciseDataSource;
  private final Object exerciseDataSourceLock = new Object();

  /**
   * Instantiates a new view model for the main object.
   */
  public MainViewModel() {
    courseViewModel.addValueObserver(this, MainViewModel::updateExercises);
  }

  public void setExerciseDataSource(@NotNull ExerciseDataSource.Provider provider,
                                    @NotNull ExerciseDataSource.AuthProvider authProvider) {
    setExerciseDataSource(provider.create(authProvider));
    updateExercises();
  }

  private void setExerciseDataSource(@Nullable ExerciseDataSource exerciseDataSource) {
    synchronized (exerciseDataSourceLock) {
      this.exerciseDataSource = exerciseDataSource;
    }
  }

  /**
   * Returns data source object for exercises.
   *
   * @return An {@link ExerciseDataSource} object.
   */
  @Nullable
  public ExerciseDataSource getExerciseDataSource() {
    synchronized (exerciseDataSourceLock) {
      return exerciseDataSource;
    }
  }

  public void clear() {
    setExerciseDataSource(null);
  }

  private void updateExercises() {
    ExerciseDataSource localExerciseDataSource = getExerciseDataSource();
    CourseViewModel course = courseViewModel.get();
    if (course == null || localExerciseDataSource == null) {
      return;
    }
    try {
      exercisesViewModel.set(
          new ExercisesTreeViewModel(localExerciseDataSource.getExerciseGroups(course.getModel())));
    } catch (InvalidAuthenticationException e) {
      // TODO: might want to communicate this to the user somehow
    } catch (IOException e) {
      // This too
    }
  }

  @NotNull
  public ObservableProperty<CourseViewModel> getCourseViewModel() {
    return courseViewModel;
  }
}
