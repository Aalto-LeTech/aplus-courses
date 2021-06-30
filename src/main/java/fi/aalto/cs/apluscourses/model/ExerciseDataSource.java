package fi.aalto.cs.apluscourses.model;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ExerciseDataSource {

  @NotNull
  SubmissionInfo getSubmissionInfo(@NotNull Exercise exercise,
                                   @NotNull Authentication authentication) throws IOException;

  @NotNull
  SubmissionHistory getSubmissionHistory(@NotNull Exercise exercise,
                                         @NotNull Authentication authentication) throws IOException;

  @NotNull
  List<Group> getGroups(@NotNull Course course, @NotNull Authentication authentication)
      throws IOException;

  @NotNull
  List<ExerciseGroup> getExerciseGroups(@NotNull Course course,
                                        @NotNull Points points,
                                        @NotNull Map<Long, Tutorial> tutorials,
                                        @NotNull Authentication authentication) throws IOException;

  @NotNull
  Points getPoints(@NotNull Course course, @NotNull Authentication authentication)
      throws IOException;

  @NotNull
  SubmissionResult getSubmissionResult(@NotNull String submissionUrl,
                                       @NotNull Exercise exercise,
                                       @NotNull Authentication authentication,
                                       @NotNull ZonedDateTime minCacheEntryTime) throws IOException;

  @NotNull
  User getUser(@NotNull Authentication authentication) throws IOException;

  @Nullable
  Student getStudent(@NotNull Course course,
                     @NotNull Authentication authentication,
                     long id) throws IOException;

  @Nullable
  Student getStudent(@NotNull Authentication authentication,
                     @NotNull String url,
                     long id) throws IOException;

  @NotNull
  ZonedDateTime getEndingTime(@NotNull Course course,
                              @NotNull Authentication authentication) throws IOException;

  @Nullable
  String submit(@NotNull Submission submission, @NotNull Authentication authentication)
      throws IOException;
}
