package fi.aalto.cs.apluscourses.ui.exercise;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionViewModel;
import fi.aalto.cs.apluscourses.ui.GuiObject;
import fi.aalto.cs.apluscourses.ui.base.CheckBox;
import fi.aalto.cs.apluscourses.ui.base.OurComboBox;
import fi.aalto.cs.apluscourses.ui.base.OurDialogWrapper;
import java.awt.Color;
import java.awt.event.ItemEvent;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubmissionDialog extends OurDialogWrapper {

  @NotNull
  private final SubmissionViewModel viewModel;

  private JPanel basePanel;

  protected JLabel exerciseName;

  protected OurComboBox<Group> groupComboBox;

  protected CheckBox defaultGroupCheckBox;

  protected JLabel submissionCount;

  @GuiObject
  private JLabel filenames;
  private JLabel warning;
  private JLabel groupWarning;

  /**
   * Construct a submission dialog with the given view model.
   */
  public SubmissionDialog(@NotNull SubmissionViewModel viewModel, @Nullable Project project) {
    super(project);

    this.viewModel = viewModel;

    setTitle(getText("ui.toolWindow.subTab.exercises.submission.submitExercise"));

    groupComboBox.selectedItemBindable.bindToSource(viewModel.selectedGroup);
    registerValidationItem(groupComboBox.selectedItemBindable);

    defaultGroupCheckBox.isCheckedBindable.bindToSource(viewModel.makeDefaultGroup);

    warning.setText(viewModel.getSubmissionWarning(project));

    groupWarning.setText("<html><body>You have previously submitted this assignment in a different group.<br>" +
        "Changing the group might cause the submission to fail.</body></html>");
    groupWarning.setForeground(new JBColor(new Color(192, 96, 0), new Color(192, 192, 0)));

    init();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return basePanel;
  }

  @NotNull
  @Override
  protected Action @NotNull [] createActions() {
    return new Action[] {getOKAction(), getCancelAction()};
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    exerciseName = new JLabel("<html><body><h2>" + viewModel.getPresentableExerciseName()
        + "</h2></body></html>");

    groupComboBox =
        new OurComboBox<>(viewModel.getAvailableGroups().toArray(new Group[0]), Group.class);
    groupComboBox.setRenderer(new GroupRenderer());
    groupComboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        Group selectedGroup = (Group) e.getItem();
        groupWarning.setVisible(!viewModel.isAbleToSubmitWithGroup(selectedGroup));

        // we need the resize operation to execute after this handler is done executing
        // otherwise the setVisible change won't be picked up by the layout manager
        SwingUtilities.invokeLater(this::pack);
      }
    });

    StringBuilder filenamesHtml = new StringBuilder("<html><body>Files:<ul>");
    for (SubmittableFile file : viewModel.getFiles()) {
      filenamesHtml.append("<li>").append(viewModel.getFileInformationText(file)).append("</li>");
    }
    filenamesHtml.append("</ul></body></html>");
    filenames = new JLabel(filenamesHtml.toString());

    submissionCount = new JLabel(viewModel.getSubmissionCountText());
  }

}
