package fi.aalto.cs.apluscourses.ui.exercise;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionViewModel;
import fi.aalto.cs.apluscourses.ui.GuiObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubmissionDialog extends DialogWrapper {

  private SubmissionViewModel viewModel;
  private JPanel basePanel;

  protected JLabel exerciseName;

  protected JComboBox<Group> groupComboBox;

  protected JLabel submissionCount;

  @GuiObject
  private JLabel filenames;

  /**
   * Construct a submission dialog with the given view model.
   */
  public SubmissionDialog(@NotNull SubmissionViewModel viewModel) {
    super(viewModel.getProject());
    this.viewModel = viewModel;
    setTitle("Submit Exercise");
    setButtonsAlignment(SwingConstants.CENTER);
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

  @Nullable
  @Override
  protected ValidationInfo doValidate() {
    Group selectedGroup = (Group) groupComboBox.getSelectedItem();
    if (selectedGroup.getId() == -1) {
      return new ValidationInfo("Select a group", groupComboBox);
    }
    return null;
  }

  @Override
  protected void doOKAction() {
    viewModel.setGroup((Group) groupComboBox.getSelectedItem());
    super.doOKAction();
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    exerciseName = new JLabel("<html><body><h2>" + viewModel.getPresentableExerciseName()
        + "</h2></body></html>");

    // We make a copy of the list as we are modifying it
    List<Group> availableGroups = new ArrayList<>(viewModel.getAvailableGroups());
    availableGroups.add(0,
        new Group(-1, Collections.singletonList("Select group...")));
    groupComboBox = new ComboBox<>(availableGroups.stream().toArray(Group[]::new));
    groupComboBox.setRenderer(new GroupRenderer());

    StringBuilder filenamesHtml = new StringBuilder("<html><body>Files:<ul>");
    viewModel.getFilenames().forEach(filename -> filenamesHtml.append("<li>" + filename + "</li>"));
    filenamesHtml.append("</ul></body></html>");
    filenames = new JLabel(filenamesHtml.toString());

    submissionCount = new JLabel("You are about to make submission "
        + (viewModel.getNumberOfSubmissions() + 1) + " out of "
        + viewModel.getMaxNumberOfSubmissions() + ".");
  }

  @FunctionalInterface
  public interface Factory {
    SubmissionDialog createDialog(@NotNull SubmissionViewModel viewModel);
  }
}
