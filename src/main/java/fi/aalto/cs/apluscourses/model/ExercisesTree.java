package fi.aalto.cs.apluscourses.model;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExercisesTree {
  private final List<ExerciseGroup> exerciseGroups;
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
}
