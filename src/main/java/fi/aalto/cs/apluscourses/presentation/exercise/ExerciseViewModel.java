package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.base.TreeViewModel;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExerciseViewModel extends SelectableNodeViewModel<Exercise> implements TreeViewModel {

  public ExerciseViewModel(@NotNull Exercise exercise) {
    super(exercise);
  }

  public String getPresentableName() {
    return APlusLocalizationUtil.getEnglishName(getModel().getName());
  }

  /**
   * Returns {@code true} if the exercise is submittable from the plugin, {@code false} otherwise.
   */
  public boolean isSubmittable() {
    String presentableName = getPresentableName();
    return presentableName.length() > "Assignment xx (".length()
        && !"Assignment 1 (Piazza)".equals(presentableName);
  }

  @Nullable
  @Override
  public List<TreeViewModel> getSubtrees() {
    return null;
  }
}
