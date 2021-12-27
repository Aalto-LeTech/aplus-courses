package fi.aalto.cs.apluscourses.model;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExercisesTree {
  @NotNull
  private final List<ExerciseGroup> exerciseGroups;

  @Nullable
  private final Student selectedStudent;

  public ExercisesTree() {
    this(Collections.emptyList(), null);
  }

  public ExercisesTree(@NotNull List<ExerciseGroup> exerciseGroups) {
    this(exerciseGroups, null);
  }

  public ExercisesTree(@NotNull List<ExerciseGroup> exerciseGroups,
                       @Nullable Student selectedStudent) {
    this.exerciseGroups = exerciseGroups;
    this.selectedStudent = selectedStudent;
  }

  @NotNull
  public List<ExerciseGroup> getExerciseGroups() {
    return exerciseGroups;
  }

  @Nullable
  public Student getSelectedStudent() {
    return selectedStudent;
  }

  /**
   * Finds the exercise from the given url, returns null if not found.
   */
  @Nullable
  public Exercise findExerciseByUrl(@NotNull String htmlUrl) {
    return exerciseGroups.stream()
        .flatMap(group -> group.getExercises().stream())
        .filter(exercise -> exercise.getHtmlUrl().equals(htmlUrl))
        .findFirst()
        .orElse(null);
  }
}
