package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.APlusAuthentication;
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
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubmissionViewModel {

  private final Exercise exercise;

  private final SubmissionInfo submissionInfo;

  private final SubmissionHistory submissionHistory;

  private final List<Group> availableGroups;

  private final APlusAuthentication authentication;

  private final Path[] filePaths;
  @NotNull
  private final Submission.Submitter submitter;

  public final ObservableProperty<Group> selectedGroup =
      new ObservableReadWriteProperty<>(null, SubmissionViewModel::validateGroupSelection);

  public final ObservableProperty<String> selectedModule =
      new ObservableReadWriteProperty<>(null, SubmissionViewModel::validateModuleSelection);

  @Nullable
  private static ValidationError validateModuleSelection(@Nullable String module) {
    return module == null ? new ModuleNotSelectedError() : null;
  }

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
                             @NotNull APlusAuthentication authentication,
                             @NotNull Path[] filePaths,
                             @NotNull Submission.Submitter submitter) {
    this.exercise = exercise;
    this.submissionInfo = submissionInfo;
    this.submissionHistory = submissionHistory;
    this.availableGroups = availableGroups;
    this.authentication = authentication;
    this.filePaths = filePaths;
    this.submitter = submitter;
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
    return submissionInfo.getFiles();
  }

  public int getCurrentSubmissionNumber() {
    return submissionHistory.getNumberOfSubmissions() + 1;
  }

  public int getMaxNumberOfSubmissions() {
    return submissionInfo.getSubmissionsLimit();
  }

  public Submission buildSubmission() {
    Group group = Objects.requireNonNull(selectedGroup.get());
    return new Submission(exercise, submissionInfo, filePaths, authentication, group, submitter);
  }

  public ValidationError validateSubmissionCount() {
    return getCurrentSubmissionNumber() > getMaxNumberOfSubmissions()
        ? new MaxNumberOfSubmissionsExceededError() : null;
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


  public static class ModuleNotSelectedError implements ValidationError {

    @NotNull
    @Override
    public String getDescription() {
      return "Select a module";
    }
  }
}
