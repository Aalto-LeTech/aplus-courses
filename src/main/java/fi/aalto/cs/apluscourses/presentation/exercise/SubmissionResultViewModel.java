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

  private final ExerciseViewModel parent;

  private final int submissionNumber;

  /**
   * Construct a view model corresponding to the given submission result.
   */
  public SubmissionResultViewModel(@NotNull ExerciseViewModel parent,
                                   @NotNull SubmissionResult submissionResult,
                                   int submissionNumber) {
    super(submissionResult);
    this.parent = parent;
    this.submissionNumber = submissionNumber;
  }

  @NotNull
  public String getPresentableName() {
    return getText("presentation.submissionResultViewModel.nameStart") + " " + submissionNumber;
  }

  @NotNull
  public String getStatusText() {
    return getModel().getPoints() + "/" + parent.getModel().getMaxPoints() + " points";
  }

  @Nullable
  @Override
  public List<? extends TreeViewModel> getSubtrees() {
    return null;
  }
}
