package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import org.jetbrains.annotations.NotNull;

public class ExerciseViewModel extends SelectableNodeViewModel<Exercise> {

  public ExerciseViewModel(@NotNull Exercise exercise) {
    super(exercise);
  }

  public String getPresentableName() {
    return APlusLocalizationUtil.getEnglishName(getModel().getName());
  }

}
