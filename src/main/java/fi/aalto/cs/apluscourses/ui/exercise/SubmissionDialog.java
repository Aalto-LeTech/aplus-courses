package fi.aalto.cs.apluscourses.ui.exercise;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionViewModel;
import fi.aalto.cs.apluscourses.ui.GuiObject;
import fi.aalto.cs.apluscourses.ui.base.OurComboBox;
import fi.aalto.cs.apluscourses.ui.base.OurDialogWrapper;
import java.util.List;
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

  @GuiObject
  private JLabel exerciseName;

  private OurComboBox<Group> groupComboBox;

  @GuiObject
  private JLabel submissionCount;

  @GuiObject
  private JLabel filenames;

  private JLabel ioException;

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
    ioException.setText(viewModel.getIoExceptionText());

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

    List<Group> availableGroups = viewModel.getAvailableGroups();
    groupComboBox = new OurComboBox<>(availableGroups.stream().toArray(Group[]::new), Group.class);
    groupComboBox.setRenderer(new GroupRenderer());

    StringBuilder filenamesHtml = new StringBuilder("<html><body>Files:<ul>");
    viewModel.getFiles().forEach(file -> filenamesHtml.append("<li>" + file.getName() + "</li>"));
    filenamesHtml.append("</ul></body></html>");
    filenames = new JLabel(filenamesHtml.toString());

    submissionCount = new JLabel("You are about to make submission "
        + (viewModel.getNumberOfSubmissions() + 1) + " out of "
        + viewModel.getMaxNumberOfSubmissions() + ".");
  }
}
