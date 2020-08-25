package fi.aalto.cs.apluscourses.presentation.exercise;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.Submission;
import fi.aalto.cs.apluscourses.model.SubmissionHistory;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import fi.aalto.cs.apluscourses.utils.observable.ValidationError;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubmissionViewModel {

  private final Exercise exercise;

  private final SubmissionInfo submissionInfo;

  private final SubmissionHistory submissionHistory;

  private final List<Group> availableGroups;

  private final Map<String, Path> filePaths;

  private final SubmittableFile[] submittableFiles;

  private final String language;

  public final ObservableProperty<Group> selectedGroup =
      new ObservableReadWriteProperty<>(null, SubmissionViewModel::validateGroupSelection);

  @Nullable
  private static ValidationError validateGroupSelection(@Nullable Group group) {
    return group == null ? new GroupNotSelectedError() : null;
  }

  /**
   * Construct a submission view model with the given exercise, groups, authentication, and project.
   */
  public SubmissionViewModel(@NotNull Exercise exercise,
                             @NotNull SubmissionInfo submissionInfo,
                             @NotNull SubmissionHistory submissionHistory,
                             @NotNull List<Group> availableGroups,
                             @NotNull Map<String, Path> filePaths,
                             @NotNull String language) {
    this.exercise = exercise;
    this.submissionInfo = submissionInfo;
    this.submissionHistory = submissionHistory;
    this.availableGroups = availableGroups;
    this.filePaths = filePaths;
    this.language = language;
    this.submittableFiles = submissionInfo.getFiles(language).toArray(new SubmittableFile[0]);
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
  public SubmittableFile[] getFiles() {
    return submittableFiles;
  }

  /**
   * Returns a string describing which submission the user is about to make and what the submission
   * limit is (if it exists).
   */
  @NotNull
  public String getSubmissionCountText() {
    StringBuilder submissionCountText = new StringBuilder("You are about to make submission ");
    submissionCountText.append(submissionHistory.getNumberOfSubmissions() + 1);
    if (submissionInfo.getSubmissionsLimit() != 0) {
      submissionCountText.append(" out of ");
      submissionCountText.append(submissionInfo.getSubmissionsLimit());
    }
    submissionCountText.append('.');
    return submissionCountText.toString();
  }

  /**
   * Warning text if max submission number is exceeded or close to be exceeded.
   *
   * @return A warning text or null, if no warning.
   */
  @Nullable
  public String getSubmissionWarning() {
    if (submissionInfo.getSubmissionsLimit() == 0) {
      return null;
    }
    int submissionsLeft =
        submissionInfo.getSubmissionsLimit() - submissionHistory.getNumberOfSubmissions();
    if (submissionsLeft == 1) {
      return getText("presentation.submissionViewModel.warning.lastSubmission");
    }
    if (submissionsLeft <= 0) {
      return getText("presentation.submissionViewModel.warning.submissionsExceeded");
    }
    return null;
  }

  public Submission buildSubmission() {
    Group group = Objects.requireNonNull(selectedGroup.get());
    return new Submission(exercise, submissionInfo, filePaths, group, language);
  }

  public static class GroupNotSelectedError implements ValidationError {

    @NotNull
    @Override
    public String getDescription() {
      return getText("presentation.submissionViewModel.selectAGroup");
    }
  }
}
