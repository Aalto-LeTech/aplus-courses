package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.Submission;
import fi.aalto.cs.apluscourses.model.SubmissionHistory;
import fi.aalto.cs.apluscourses.model.SubmittableExercise;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import fi.aalto.cs.apluscourses.utils.observable.ValidationError;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubmissionViewModel {

  private final SubmittableExercise exercise;

  private final SubmissionHistory submissionHistory;

  private final List<Group> availableGroups;

  private final APlusAuthentication authentication;

  private final Path[] filePaths;

  public final ObservableProperty<Group> selectedGroup =
      new ObservableReadWriteProperty<>(null, SubmissionViewModel::validateGroupSelection);

  private final IOException ioException;

  /**
   * Construct a submission view model with the given exercise, groups, authentication, and project.
   */
  public SubmissionViewModel(@NotNull SubmittableExercise exercise,
                             @NotNull SubmissionHistory submissionHistory,
                             @NotNull List<Group> availableGroups,
                             @NotNull APlusAuthentication authentication,
                             @NotNull Path[] filePaths,
                             @Nullable IOException ioException) {
    this.exercise = exercise;
    this.submissionHistory = submissionHistory;
    this.availableGroups = availableGroups;
    this.authentication = authentication;
    this.filePaths = filePaths;
    this.ioException = ioException;
  }

  private static ValidationError validateGroupSelection(Group group) {
    return group == null ? new GroupNotSelectedError() : null;
  }

  @NotNull
  public String getPresentableExerciseName() {
    return APlusLocalizationUtil.getEnglishName(exercise.getName());
  }

  @NotNull
  public List<Group> getAvailableGroups() {
    return availableGroups;
  }

  @NotNull
  public List<SubmittableFile> getFiles() {
    return exercise.getFiles();
  }

  public int getCurrentSubmissionNumber() {
    return submissionHistory.getNumberOfSubmissions() + 1;
  }

  public int getMaxNumberOfSubmissions() {
    return exercise.getSubmissionsLimit();
  }

  @NotNull
  public Submission buildSubmission() {
    Group group = Objects.requireNonNull(selectedGroup.get());
    return new Submission(exercise, filePaths, authentication, group);
  }

  public ValidationError validateSubmissionCount() {
    return getCurrentSubmissionNumber() > getMaxNumberOfSubmissions()
        ? new MaxNumberOfSubmissionsExceededError() : null;
  }

  @NotNull
  public String getIoExceptionText() {
    return ioException == null ? "" : "Upload failed: " + ioException.getLocalizedMessage();
  }

  public static class MaxNumberOfSubmissionsExceededError implements ValidationError {

    @NotNull
    @Override
    public String getDescription() {
      return "Max. number of submissions exceeded";
    }
  }

  public static class GroupNotSelectedError implements ValidationError {

    @NotNull
    @Override
    public String getDescription() {
      return "Select a group";
    }
  }
}
