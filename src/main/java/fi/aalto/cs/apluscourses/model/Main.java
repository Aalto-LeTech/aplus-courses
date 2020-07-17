package fi.aalto.cs.apluscourses.model;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Main {

  private final ExerciseDataSource exerciseDataSource;
  private volatile Course course;
  private volatile List<ExerciseGroup> exerciseGroups;

  public Main(@NotNull ExerciseDataSource exerciseDataSource) {
    this.exerciseDataSource = exerciseDataSource;
  }

  @Nullable
  public Course getCourse() {
    return course;
  }

  public void setCourse(Course course) {
    this.course = course;
  }

  @Nullable
  public List<ExerciseGroup> getExerciseGroups() {
    return exerciseGroups;
  }

  public void setExerciseGroups(List<ExerciseGroup> exerciseGroups) {
    this.exerciseGroups = exerciseGroups;
  }

  @NotNull
  public ExerciseDataSource getExerciseDataSource() {
    return exerciseDataSource;
  }
}
