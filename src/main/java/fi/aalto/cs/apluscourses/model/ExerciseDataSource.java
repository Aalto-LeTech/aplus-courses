package fi.aalto.cs.apluscourses.model;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ExerciseDataSource {
  @NotNull
  String getApiUrl();

  @NotNull
  List<Group> getGroups(@NotNull Course course, @NotNull Authentication authentication)
      throws IOException;

  @NotNull
  List<ExerciseGroup> getExerciseGroups(@NotNull Course course,
                                        @NotNull Points points,
                                        @NotNull Authentication authentication) throws IOException;

  @NotNull
  Points getPoints(@NotNull Course course, @NotNull Authentication authentication)
      throws IOException;

  void prefetchExercise(long exerciseId, @NotNull Authentication authentication);

  @NotNull
  Exercise getExercise(long exerciseId,
                       @NotNull Points points,
                       @NotNull Authentication authentication,
                       @NotNull ZonedDateTime minCacheEntryTime) throws IOException;

  @NotNull
  SubmissionResult getSubmissionResult(@NotNull String submissionUrl,
                                       @NotNull Exercise exercise,
                                       @NotNull Authentication authentication,
                                       @NotNull ZonedDateTime minCacheEntryTime) throws IOException;

  @Nullable
  String submit(@NotNull Submission submission, @NotNull Authentication authentication)
      throws IOException;
}
