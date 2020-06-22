package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import org.jetbrains.annotations.NotNull;

public class ExerciseViewModel {

  @NotNull
  private final Exercise exercise;

  public ExerciseViewModel(@NotNull Exercise exercise) {
    this.exercise = exercise;
  }

  public String getPresentableName() {
    return APlusLocalizationUtil.getEnglishName(exercise.getName());
  }

}
