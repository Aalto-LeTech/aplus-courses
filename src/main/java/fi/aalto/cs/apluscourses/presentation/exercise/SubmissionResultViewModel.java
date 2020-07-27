package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.base.TreeViewModel;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubmissionResultViewModel extends SelectableNodeViewModel<SubmissionResult>
    implements TreeViewModel {

  public SubmissionResultViewModel(@NotNull SubmissionResult submissionResult) {
    super(submissionResult);
  }

  public String getPresentableName() {
    return "Submission " + getModel().getSubmissionNumber();
  }

  @Nullable
  @Override
  public List<? extends TreeViewModel> getSubtrees() {
    return null;
  }
}
