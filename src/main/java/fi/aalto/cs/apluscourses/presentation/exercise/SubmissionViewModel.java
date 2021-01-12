package fi.aalto.cs.apluscourses.presentation.exercise;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.Submission;
import fi.aalto.cs.apluscourses.model.SubmissionHistory;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import fi.aalto.cs.apluscourses.utils.FileDateFormatter;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import fi.aalto.cs.apluscourses.utils.observable.ValidationError;

import java.io.IOException;
import java.nio.file.Files;
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

  public final ObservableProperty<Boolean> makeDefaultGroup =
      new ObservableReadWriteProperty<>(false);

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
                             @Nullable Group defaultGroup,
                             @NotNull Map<String, Path> filePaths,
                             @NotNull String language) {
    this.exercise = exercise;
    this.submissionInfo = submissionInfo;
    this.submissionHistory = submissionHistory;
    this.availableGroups = availableGroups;
    this.filePaths = filePaths;
    this.language = language;
    this.submittableFiles = submissionInfo.getFiles(language).toArray(new SubmittableFile[0]);
    if (defaultGroup != null) {
      selectedGroup.set(defaultGroup);
      makeDefaultGroup.set(true);
    }
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

  public int getCurrentSubmissionNumber() {
    return submissionHistory.getNumberOfSubmissions() + 1;
  }

  /**
   * Returns a string describing which submission the user is about to make and what the submission
   * limit is (if it exists).
   */
  @NotNull
  public String getSubmissionCountText() {
    StringBuilder submissionCountText = new StringBuilder("You are about to make submission ");
    submissionCountText.append(getCurrentSubmissionNumber());
    if (submissionInfo.getSubmissionsLimit() != 0) {
      submissionCountText.append(" out of ");
      submissionCountText.append(submissionInfo.getSubmissionsLimit());
    }
    submissionCountText.append('.');
    return submissionCountText.toString();
  }

  /**
   * Formats a descriptive string for the submission dialog about a submittable file. The string
   * includes the file name and its last modification date.
   * @param file The submittable file in question.
   */
  @NotNull
  public String getFileInformationText(SubmittableFile file) {
    StringBuilder fileInfoText = new StringBuilder(file.getName());
    try {
      String lastModificationTime =
              FileDateFormatter.getFileModificationTime(filePaths.get(file.getKey()));
      fileInfoText.append(" (modified ").append(lastModificationTime).append(")");
    } catch (IOException e) {
      // don't print anything about the last modification time if a very unlikely exception happened
    }
    return fileInfoText.toString();
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
