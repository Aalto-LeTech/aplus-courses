package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.model.User;
import fi.aalto.cs.apluscourses.presentation.exercise.EmptyExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import fi.aalto.cs.apluscourses.presentation.ideactivities.TutorialViewModel;
import fi.aalto.cs.apluscourses.presentation.news.NewsTreeViewModel;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainViewModel {

  public static final Logger logger = LoggerFactory.getLogger(MainViewModel.class);

  public final Event disposing = new Event();

  @NotNull
  public final ToolWindowCardViewModel toolWindowCardViewModel = new ToolWindowCardViewModel();

  @NotNull
  public final ObservableProperty<CourseViewModel> courseViewModel =
      new ObservableReadWriteProperty<>(null);

  @NotNull
  public final ObservableProperty<ExercisesTreeViewModel> exercisesViewModel =
      new ObservableReadWriteProperty<>(new EmptyExercisesTreeViewModel());

  @NotNull
  public final ObservableProperty<NewsTreeViewModel> newsTreeViewModel =
      new ObservableReadWriteProperty<>(null);

  @NotNull
  public final ProgressViewModel progressViewModel = new ProgressViewModel();

  @NotNull
  public final ObservableProperty<BannerViewModel> bannerViewModel =
      new ObservableReadWriteProperty<>(null);

  @NotNull
  public final ObservableProperty<TutorialViewModel> tutorialViewModel =
      new ObservableReadWriteProperty<>(null);

  @NotNull
  private final Options exerciseFilterOptions;

  @Nullable
  private String feedbackCss;

  /**
   * Instantiates a class representing the whole main view of the plugin.
   */
  public MainViewModel(@NotNull Options exerciseFilterOptions) {
    this.exerciseFilterOptions = exerciseFilterOptions;
  }

  /**
   * Creates a new {@link ExercisesTreeViewModel} with the given exercise groups, which is then set
   * to {@link MainViewModel#exercisesViewModel}.
   */
  public void updateExercisesViewModel(@NotNull CourseProject courseProject) {
    exercisesViewModel.set(
        ExercisesTreeViewModel.createExerciseTreeViewModel(courseProject.getExerciseTree(), exerciseFilterOptions,
            courseProject));
  }

  /**
   * Creates a new {@link NewsTreeViewModel} from the NewsTree from the {@link CourseProject},
   * which is then set to {@link MainViewModel#newsTreeViewModel}.
   */
  public void updateNewsViewModel(@NotNull CourseProject courseProject) {
    if (courseProject.getNewsTree() == null) {
      newsTreeViewModel.set(null);
    } else {
      newsTreeViewModel.set(new NewsTreeViewModel(courseProject.getNewsTree(), this));
    }
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
   * initialized (by InitializationActivity).
   */
  public void setProjectReady(boolean isReady) {
    toolWindowCardViewModel.setProjectReady(isReady);
  }

  /**
   * Sets the ToolWindowCardCViewModel authenticated.
   */
  public void setAuthenticated(boolean authenticated) {
    toolWindowCardViewModel.setAuthenticated(authenticated);
  }

  public void userChanged(@Nullable User user) {
    setAuthenticated(user != null);
  }

  @Nullable
  public String getFeedbackCss() {
    return feedbackCss;
  }

  public void setFeedbackCss(@Nullable String feedbackCss) {
    this.feedbackCss = feedbackCss;
  }

}
