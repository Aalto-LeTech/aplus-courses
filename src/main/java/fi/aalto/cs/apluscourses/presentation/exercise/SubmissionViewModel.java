package fi.aalto.cs.apluscourses.presentation.exercise;

import com.intellij.openapi.module.Module;
import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.Submission;
import fi.aalto.cs.apluscourses.model.SubmissionHistory;
import fi.aalto.cs.apluscourses.model.SubmittableExercise;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
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

  public final ObservableProperty<Group> selectedGroup = new ObservableReadWriteProperty<>(null);

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

    selectedGroup.setValidator(SubmissionViewModel::validateGroupSelection);
  }

  private static String validateGroupSelection(Group group) {
    return group == null ? "Select a group" : null;
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

  public int getNumberOfSubmissions() {
    return submissionHistory.getNumberOfSubmissions();
  }

  public int getMaxNumberOfSubmissions() {
    return exercise.getSubmissionsLimit();
  }

  @NotNull
  public Submission buildSubmission() {
    Group group = Objects.requireNonNull(selectedGroup.get());
    return new Submission(exercise, filePaths, authentication, group);
  }

  public String validateSubmissionCount() {
    return getNumberOfSubmissions() >= getMaxNumberOfSubmissions()
        ? "Max. number of submissions exceeded" : null;
  }

  @NotNull
  public String getIoExceptionText() {
    return ioException == null ? "" : "Upload failed: " + ioException.getLocalizedMessage();
  }
}
