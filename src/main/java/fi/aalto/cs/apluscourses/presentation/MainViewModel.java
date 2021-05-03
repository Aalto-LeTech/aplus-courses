package fi.aalto.cs.apluscourses.presentation;

import static fi.aalto.cs.apluscourses.dal.APlusTokenAuthentication.APLUS_USER;

import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import fi.aalto.cs.apluscourses.dal.TokenAuthentication;
import fi.aalto.cs.apluscourses.intellij.dal.IntelliJPasswordStorage;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.presentation.exercise.EmptyExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import fi.aalto.cs.apluscourses.presentation.ideactivities.TutorialViewModel;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.util.List;
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
      new ObservableReadWriteProperty<>(new EmptyExercisesTreeViewModel());

  @NotNull
  public final ObservableProperty<TutorialViewModel> tutorialViewModel =
      new ObservableReadWriteProperty<>(null);


  @NotNull
  public final ObservableProperty<Authentication> authentication =
      new ObservableReadWriteProperty<>(null);

  @NotNull
  private String userName = "";

  @NotNull
  private final Options exerciseFilterOptions;

  /**
   * Instantiates a class representing the whole main view of the plugin.
   */
  public MainViewModel(@NotNull Options exerciseFilterOptions) {
    this.exerciseFilterOptions = exerciseFilterOptions;
    courseViewModel.addSimpleObserver(this, MainViewModel::updateExercises);
    authentication.addSimpleObserver(this, MainViewModel::updateExercises);
    authentication.addSimpleObserver(this, MainViewModel::updateUserName);
  }

  private void updateUserName() {
    Course course = null;
    var localCourseViewModel = courseViewModel.get();
    if (localCourseViewModel != null) {
      course = localCourseViewModel.getModel();
    }
    Authentication auth = authentication.get();
    if (course != null && auth != null) {
      try {
        this.userName = course.getExerciseDataSource().getUserName(auth);
      } catch (IOException e) {
        logger.error("Failed to fetch user data", e);
      }
    } else {
      this.userName = "";
    }
  }

  /**
   * Creates a new {@link ExercisesTreeViewModel} with the given exercise groups, which is then set
   * to {@link MainViewModel#exercisesViewModel}.
   */
  public void updateExercisesViewModel(@NotNull List<ExerciseGroup> exerciseGroups) {
    var viewModel = new ExercisesTreeViewModel(exerciseGroups, exerciseFilterOptions);
    viewModel.setAuthenticated(true);
    viewModel.setProjectReady(exercisesViewModel.get().isProjectReady());
    exercisesViewModel.set(viewModel);
  }

  public void dispose() {
    disposing.trigger();
  }

  @Nullable
  public ExercisesTreeViewModel getExercises() {
    return exercisesViewModel.get();
  }

  @NotNull
  public Options getExerciseFilterOptions() {
    return exerciseFilterOptions;
  }

  /**
   * Calling this method informs the main view model that the corresponding project has been
   * initialized (by InitializationActivity). If needed, this method notifies the listeners of
   * exercisesViewModel.
   */
  public void setProjectReady(boolean isReady) {
    var viewModel = exercisesViewModel.get();
    if (viewModel == null) {
      return;
    }
    var changed = viewModel.setProjectReady(isReady);
    if (changed) {
      exercisesViewModel.valueChanged();
    }
  }

  @NotNull
  public String getUserName() {
    return userName;
  }
}
