package fi.aalto.cs.apluscourses.presentation.exercise;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.base.TreeViewModel;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubmissionResultViewModel extends SelectableNodeViewModel<SubmissionResult>
    implements TreeViewModel {

  private final int submissionNumber;

  /**
   * Construct a view model corresponding to the given submission result.
   */
  public SubmissionResultViewModel(@NotNull SubmissionResult submissionResult,
                                   int submissionNumber) {
    super(submissionResult);
    this.submissionNumber = submissionNumber;
  }

  @NotNull
  public String getPresentableName() {
    return getText("presentation.submissionResultViewModel.nameStart") + " " + submissionNumber;
  }

  @Nullable
  @Override
  public List<? extends TreeViewModel> getSubtrees() {
    return null;
  }
}
