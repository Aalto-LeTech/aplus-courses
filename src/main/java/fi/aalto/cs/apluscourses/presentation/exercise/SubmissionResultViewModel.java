package fi.aalto.cs.apluscourses.presentation.exercise;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.presentation.base.BaseViewModel;
import fi.aalto.cs.apluscourses.presentation.base.SearchableNode;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import org.jetbrains.annotations.NotNull;

public class SubmissionResultViewModel extends SelectableNodeViewModel<SubmissionResult>
        implements SearchableNode {

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


  /**
   * {@summary}Constructs text appearing in parenthesis.
   *
   * @return Returns status text for assignment.
   *         Methods considers case assignment has not yet been graded
   */
  @NotNull
  public String getStatusText() {
    SubmissionResult model = getModel();
    return (model.getStatus() == SubmissionResult.Status.UNKNOWN)
            ? getText("presentation.submissionResultViewModel.inGrading")
            : getStatus(model);
  }

  private String getStatus(SubmissionResult model) {
    return model.getPoints() + "/" + model.getExercise().getMaxPoints() + " points";
  }

  @Override
  public long getId() {
    return getModel().getId();
  }
}
