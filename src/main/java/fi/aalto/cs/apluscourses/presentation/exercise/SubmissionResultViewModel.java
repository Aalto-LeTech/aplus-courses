package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.base.TreeViewModel;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubmissionResultViewModel extends SelectableNodeViewModel<SubmissionResult>
    implements TreeViewModel {

  @NotNull String submissionUrl;

  /**
   * Construct a view model corresponding to the given submission result.
   */
  public SubmissionResultViewModel(@NotNull SubmissionResult submissionResult,
                                   @NotNull String exerciseUrl) {
    super(submissionResult);
    this.submissionUrl = exerciseUrl + "submissions/" + submissionResult.getId() + "/";
  }

  @NotNull
  public String getPresentableName() {
    return "Submission " + getModel().getSubmissionNumber();
  }

  @NotNull
  public String getSubmissionUrl() {
    return submissionUrl;
  }

  @Nullable
  @Override
  public List<? extends TreeViewModel> getSubtrees() {
    return null;
  }
}
