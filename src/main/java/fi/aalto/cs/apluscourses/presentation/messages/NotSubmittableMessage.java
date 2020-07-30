package fi.aalto.cs.apluscourses.presentation.messages;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import org.jetbrains.annotations.NotNull;

public class NotSubmittableMessage implements Message {
  @NotNull
  private final Exercise exercise;

  public NotSubmittableMessage(@NotNull Exercise exercise) {
    this.exercise = exercise;
  }

  @NotNull
  @Override
  public String getContent() {
    return "'" + APlusLocalizationUtil.getEnglishName(exercise.getName())
        + "' can only be submitted from the A+ web interface.";
  }

  @NotNull
  @Override
  public String getTitle() {
    return "Cannot submit exercise";
  }

  @NotNull
  @Override
  public Level getLevel() {
    return Level.ERR;
  }

  @NotNull
  public Exercise getExercise() {
    return exercise;
  }
}
