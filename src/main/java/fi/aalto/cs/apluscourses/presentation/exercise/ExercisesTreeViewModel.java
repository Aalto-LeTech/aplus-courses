package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.presentation.base.BaseViewModel;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.base.TreeViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Option;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import fi.aalto.cs.apluscourses.utils.Event;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExercisesTreeViewModel extends BaseViewModel<List<ExerciseGroup>>
    implements TreeViewModel {

  public final Event filtered = new Event();

  @NotNull
  private final List<ExerciseGroupViewModel> groupViewModels;

  @NotNull
  private final Executor filterExecutor;

  @NotNull
  private final Options options;

  /**
   * Construct an exercises tree view model from the given exercise groups.
   */
  public ExercisesTreeViewModel(@NotNull List<ExerciseGroup> exerciseGroups,
                                @NotNull Executor filterExecutor) {
    super(exerciseGroups);
    this.groupViewModels = exerciseGroups
        .stream()
        .map(ExerciseGroupViewModel::new)
        .collect(Collectors.toList());
    this.filterExecutor = filterExecutor;
    this.options = new ExerciseFilterOptions();
    this.options.optionsChanged.addListener(this, ExercisesTreeViewModel::filter);
  }

  private void filter() {
    filterExecutor.execute(this::filterInBackground);
  }

  private void filterInBackground() {
    getChildren().stream().forEach(group -> group.applyFilter(options));
    filtered.trigger();
  }

  @NotNull
  public Options getFilterOptions() {
    return options;
  }

  /**
   * Returns the exercise from this tree that is selected, or null if no exercise is selected.
   */
  @Nullable
  public ExerciseViewModel getSelectedExercise() {
    return getChildren().parallelStream()
        .map(ExerciseGroupViewModel::getChildren)
        .flatMap(List::parallelStream)
        .filter(SelectableNodeViewModel::isSelected)
        .map(ExerciseViewModel.class::cast)
        .findFirst()
        .orElse(null);
  }

  /**
   * Returns the submission from this tree that is selected, or null if no submission is selected.
   */
  @Nullable
  public SubmissionResultViewModel getSelectedSubmission() {
    return getChildren().parallelStream()
        .map(ExerciseGroupViewModel::getChildren)
        .flatMap(List::parallelStream)
        .map(SelectableNodeViewModel::getChildren)
        .flatMap(List::parallelStream)
        .filter(SelectableNodeViewModel::isSelected)
        .map(SubmissionResultViewModel.class::cast)
        .findFirst()
        .orElse(null);
  }

  @Override
  public @NotNull List<ExerciseGroupViewModel> getChildren() {
    return groupViewModels;
  }
}
