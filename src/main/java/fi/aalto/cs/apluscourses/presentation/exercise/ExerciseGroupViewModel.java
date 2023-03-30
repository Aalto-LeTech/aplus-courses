package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.model.ExercisesLazyLoader;
import fi.aalto.cs.apluscourses.presentation.base.Searchable;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExerciseGroupViewModel extends SelectableNodeViewModel<ExerciseGroup>
    implements Searchable {

  private final @Nullable ExercisesLazyLoader exercisesLazyLoader;

  public ExerciseGroupViewModel(@NotNull ExerciseGroup exerciseGroup) {
    this(exerciseGroup, null);
  }

  /**
   * Construct an exercise group view model with the given exercise group.
   */
  public ExerciseGroupViewModel(@NotNull ExerciseGroup exerciseGroup,
                                @Nullable ExercisesLazyLoader exercisesLazyLoader) {
    super(exerciseGroup, exerciseGroup
        .getExercises()
        .stream()
        .map(ExerciseViewModel::new)
        .collect(Collectors.toList()));
    this.exercisesLazyLoader = exercisesLazyLoader;
  }

  public String getPresentableName() {
    return getModel().getName();
  }

  @Override
  protected void setVisibilityByFilterResult(Optional<Boolean> result) {
    visibility = result.orElse(true)
        && this.getChildren().stream().anyMatch(SelectableNodeViewModel::isVisible);
  }

  @Override
  public long getId() {
    return getModel().getId();
  }

  @Override
  public void willExpand() {
    if (exercisesLazyLoader != null) {
      exercisesLazyLoader.setLazyLoadedGroup(getId());
    }
  }
}
