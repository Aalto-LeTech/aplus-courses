package fi.aalto.cs.apluscourses.model;

import java.io.IOException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ExerciseDataSource {

  @NotNull
  protected final Authentication authentication;

  public ExerciseDataSource(@NotNull AuthProvider authProvider) {
    this.authentication = authProvider.create();
  }

  @NotNull
  public abstract SubmissionInfo getSubmissionInfo(@NotNull Exercise exercise) throws IOException;

  @NotNull
  public abstract SubmissionHistory getSubmissionHistory(@NotNull Exercise exercise)
      throws IOException;

  @NotNull
  public abstract List<Group> getGroups(@NotNull Course course) throws IOException;

  @NotNull
  public abstract List<ExerciseGroup> getExerciseGroups(@NotNull Course course,
                                                        @NotNull Points points) throws IOException;

  @NotNull
  public abstract Points getPoints(@NotNull Course course) throws IOException;

  public abstract String submit(Submission submission) throws IOException;

  @NotNull
  public Authentication getAuthentication() {
    return authentication;
  }

  public interface Provider {
    @Nullable
    ExerciseDataSource create(AuthProvider authentication);
  }

  public interface AuthProvider {
    @NotNull
    Authentication create();
  }
}
