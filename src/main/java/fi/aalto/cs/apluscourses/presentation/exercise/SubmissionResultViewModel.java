package fi.aalto.cs.apluscourses.presentation.exercise;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.Interfaces;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.presentation.base.Searchable;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.utils.SubmissionResultUtil;
import org.jetbrains.annotations.NotNull;

public class SubmissionResultViewModel extends SelectableNodeViewModel<SubmissionResult>
    implements Searchable {

  @NotNull
  private final Interfaces.AssistantModeProvider assistantModeProvider;

  private final int submissionNumber;

  /**
   * Construct a view model corresponding to the given submission result.
   */
  public SubmissionResultViewModel(@NotNull SubmissionResult submissionResult,
                                   int submissionNumber) {
    this(submissionResult, submissionNumber, () -> PluginSettings.getInstance().isAssistantMode());
  }

  /**
   * Construct a view model corresponding to the given submission result.
   */
  public SubmissionResultViewModel(@NotNull SubmissionResult submissionResult,
                                   int submissionNumber,
                                   @NotNull Interfaces.AssistantModeProvider assistantModeProvider) {
    super(submissionResult, null);
    this.submissionNumber = submissionNumber;
    this.assistantModeProvider = assistantModeProvider;
  }

  /**
   * Shows submission name, and if assistant mode is enabled, shows the id.
   */
  @NotNull
  public String getPresentableName() {
    return assistantModeProvider.isAssistantMode()
        ? getAndReplaceText("presentation.submissionResultViewModel.nameAssistant",
        submissionNumber, String.valueOf(getId())) :
        getAndReplaceText("presentation.submissionResultViewModel.name",
            submissionNumber);
  }


  /**
   * Constructs text appearing in parentheses.
   *
   * @return Returns status text for assignment. Method considers case assignment has not yet been graded
   */
  @NotNull
  public String getStatusText() {
    SubmissionResult model = getModel();
    return (model.getStatus() == SubmissionResult.Status.WAITING)
        ? getText("presentation.submissionResultViewModel.inGrading")
        : getStatus(model);
  }

  private String getStatus(SubmissionResult model) {
    return SubmissionResultUtil.getStatus(model);
  }

  @Override
  public @NotNull String getSearchableString() {
    return getPresentableName();
  }

  @Override
  public long getId() {
    return getModel().getId();
  }
}
