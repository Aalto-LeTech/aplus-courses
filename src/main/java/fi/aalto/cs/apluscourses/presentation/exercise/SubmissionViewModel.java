package fi.aalto.cs.apluscourses.presentation.exercise;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.SubmissionHistory;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class SubmissionViewModel {

  private Exercise exercise;

  private SubmissionInfo submissionInfo;

  private SubmissionHistory submissionHistory;

  private List<Group> availableGroups;

  private Group selectedGroup;

  private APlusAuthentication authentication;

  private Project project;

  /**
   * Construct a submission view model with the given exercise, groups, authentication, and project.
   */
  public SubmissionViewModel(@NotNull Exercise exercise,
                             @NotNull SubmissionInfo submissionInfo,
                             @NotNull SubmissionHistory submissionHistory,
                             @NotNull List<Group> availableGroups,
                             @NotNull APlusAuthentication authentication,
                             @NotNull Project project) {
    this.exercise = exercise;
    this.submissionInfo = submissionInfo;
    this.submissionHistory = submissionHistory;
    this.availableGroups = availableGroups;
    this.authentication = authentication;
    this.project = project;
  }

  @NotNull
  public Project getProject() {
    return project;
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
  public Group getSelectedGroup() {
    return selectedGroup;
  }

  public void setGroup(@NotNull Group group) {
    this.selectedGroup = group;
  }

  @NotNull
  public List<String> getFilenames() {
    return submissionInfo.getFilenames();
  }

  public int getNumberOfSubmissions() {
    return submissionHistory.getNumberOfSubmissions();
  }

  public int getMaxNumberOfSubmissions() {
    return submissionInfo.getSubmissionsLimit();
  }

}
