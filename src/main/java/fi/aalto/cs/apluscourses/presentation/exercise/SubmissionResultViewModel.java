package fi.aalto.cs.apluscourses.presentation.exercise;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import org.jetbrains.annotations.NotNull;

public class SubmissionResultViewModel extends SelectableNodeViewModel<SubmissionResult> {

  private final int submissionNumber;

  /**
   * Construct a view model corresponding to the given submission result.
   */
  public SubmissionResultViewModel(@NotNull SubmissionResult submissionResult,
                                   int submissionNumber) {
    super(submissionResult, null);
    this.submissionNumber = submissionNumber;
  }

  @NotNull
  public String getPresentableName() {
    return getText("presentation.submissionResultViewModel.nameStart") + " " + submissionNumber;
  }

  @NotNull
  public String getStatusText() {
    return getModel().getPoints() + "/" + getModel().getExercise().getMaxPoints() + " points";
  }

  @Override
  public long getId() {
    return getModel().getId();
  }
}
