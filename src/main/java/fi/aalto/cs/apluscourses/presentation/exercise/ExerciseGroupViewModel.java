package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.base.TreeViewModel;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExerciseGroupViewModel extends SelectableNodeViewModel<ExerciseGroup>
    implements TreeViewModel {

  @NotNull
  private final List<ExerciseViewModel> sortedExercises;

  /**
   * Construct an exercise group view model with the given exercise group.
   */
  public ExerciseGroupViewModel(@NotNull ExerciseGroup exerciseGroup) {
    super(exerciseGroup);
    this.sortedExercises = exerciseGroup
        .getExercises()
        .values()
        .stream()
        .sorted(Comparator.comparingLong(Exercise::getId))
        .map(ExerciseViewModel::new)
        .collect(Collectors.toList());
  }

  public String getPresentableName() {
    return APlusLocalizationUtil.getEnglishName(getModel().getName());
  }

  @NotNull
  public List<ExerciseViewModel> getExerciseViewModels() {
    return sortedExercises;
  }

  @Nullable
  @Override
  public List<ExerciseViewModel> getSubtrees() {
    return getExerciseViewModels();
  }
}
