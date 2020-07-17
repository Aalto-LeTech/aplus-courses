package fi.aalto.cs.apluscourses.model;

import java.io.IOException;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface ExerciseDataSource {

  @NotNull
  SubmissionInfo getSubmissionInfo(@NotNull Exercise exercise) throws IOException;

  @NotNull
  SubmissionHistory getSubmissionHistory(@NotNull Exercise exercise) throws IOException;

  @NotNull
  List<Group> getGroups(@NotNull Course course) throws IOException;

  @NotNull
  List<ExerciseGroup> getExerciseGroups(@NotNull Course course) throws IOException;

  @NotNull
  Authentication getAuthentication();

  void submit(Submission submission) throws IOException;
}
