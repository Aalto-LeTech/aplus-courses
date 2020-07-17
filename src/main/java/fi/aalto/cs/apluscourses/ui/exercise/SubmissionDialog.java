package fi.aalto.cs.apluscourses.ui.exercise;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionViewModel;
import fi.aalto.cs.apluscourses.ui.Dialog;
import fi.aalto.cs.apluscourses.ui.GuiObject;
import fi.aalto.cs.apluscourses.ui.base.OurComboBox;
import fi.aalto.cs.apluscourses.ui.base.OurDialogWrapper;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubmissionDialog extends OurDialogWrapper {

  @NotNull
  private final SubmissionViewModel viewModel;

  private JPanel basePanel;

  protected JLabel exerciseName;

  protected OurComboBox<Group> groupComboBox;

  protected JLabel submissionCount;

  @GuiObject
  private JLabel filenames;

  /**
   * Construct a submission dialog with the given view model.
   */
  public SubmissionDialog(@NotNull SubmissionViewModel viewModel, @Nullable Project project) {
    super(project);

    this.viewModel = viewModel;

    setTitle("Submit Exercise");
    setButtonsAlignment(SwingConstants.CENTER);

    groupComboBox.selectedItemBindable.bindToSource(viewModel.selectedGroup);
    registerValidationItem(groupComboBox.selectedItemBindable);
    registerValidationItem(viewModel::validateSubmissionCount);

    init();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return basePanel;
  }

  @NotNull
  @Override
  protected Action[] createActions() {
    return new Action[] {getOKAction(), getCancelAction()};
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    exerciseName = new JLabel("<html><body><h2>" + viewModel.getPresentableExerciseName()
        + "</h2></body></html>");

    groupComboBox =
        new OurComboBox<>(viewModel.getAvailableGroups().toArray(new Group[0]), Group.class);
    groupComboBox.setRenderer(new GroupRenderer());

    StringBuilder filenamesHtml = new StringBuilder("<html><body>Files:<ul>");
    for (SubmittableFile file : viewModel.getFiles()) {
      filenamesHtml.append("<li>").append(file.getName()).append("</li>");
    }
    filenamesHtml.append("</ul></body></html>");
    filenames = new JLabel(filenamesHtml.toString());

    submissionCount = new JLabel(String.format("You are about to make submission %d out of %d.",
        viewModel.getCurrentSubmissionNumber(), viewModel.getMaxNumberOfSubmissions()));
  }
}
