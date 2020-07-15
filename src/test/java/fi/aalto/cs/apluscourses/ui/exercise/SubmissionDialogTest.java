package fi.aalto.cs.apluscourses.ui.exercise;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.testFramework.LightIdeaTestCase;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionViewModel;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

public class SubmissionDialogTest extends LightIdeaTestCase {

  public class TestDialog extends SubmissionDialog {
    public TestDialog(@NotNull SubmissionViewModel viewModel) {
      super(viewModel);
    }

    public String getHeader() {
      return super.exerciseName.getText();
    }

    public JComboBox<Group> getGroupComboBox() {
      return super.groupComboBox;
    }

    public String getSubmissionNumberText() {
      return super.submissionCount.getText();
    }

    public Action[] getActions() {
      return super.createActions();
    }

  }

  @NotNull
  private SubmissionViewModel createMockViewModel(@NotNull String exerciseName,
                                                  @NotNull List<Group> availableGroups,
                                                  @NotNull List<String> filenames,
                                                  int numberOfSubmissions,
                                                  int maxNumberOfSubmissions) {
    SubmissionViewModel viewModel = mock(SubmissionViewModel.class);
    doReturn(null).when(viewModel).getProject();
    doReturn(exerciseName).when(viewModel).getPresentableExerciseName();
    doReturn(availableGroups).when(viewModel).getAvailableGroups();
    doReturn(filenames).when(viewModel).getFilenames();
    doReturn(numberOfSubmissions).when(viewModel).getNumberOfSubmissions();
    doReturn(maxNumberOfSubmissions).when(viewModel).getMaxNumberOfSubmissions();
    return viewModel;
  }

  @NotNull
  private TestDialog createTestDialog() {
    return new TestDialog(
        createMockViewModel(
            "Cool Name",
            Arrays.asList(new Group(123, Arrays.asList("Jarkko", "Petteri"))),
            Arrays.asList("file1", "file2"),
            3,
            10
        )
    );
  }

  @Test
  public void testSubmissionDialogTitle() {
    TestDialog testDialog = createTestDialog();
    Assert.assertEquals("The dialog has the correct title", "Submit Exercise",
        testDialog.getTitle());

  }

  @Test
  public void testSubmissionDialogHeader() {
    TestDialog testDialog = createTestDialog();
    Assert.assertThat("The header contains the exercise name", testDialog.getHeader(),
        containsString("Cool Name"));
  }

  @Test
  public void testSubmissionDialogGroupSelection() {
    TestDialog testDialog = createTestDialog();
    JComboBox<Group> comboBox = testDialog.getGroupComboBox();

    Group defaultSelection = (Group) comboBox.getSelectedItem();
    Assert.assertEquals("The default selection is no group", "Select group...",
        defaultSelection.getMemberNames().get(0));

    ComboBoxModel<Group> model = comboBox.getModel();
    Assert.assertEquals("The group selection combo box includes available groups", 123,
        model.getElementAt(1).getId());
  }

  @Test
  public void testSubmissionDialogSubmissionCountText() {
    TestDialog testDialog = createTestDialog();
    Assert.assertEquals("The submission count text is correct",
        "You are about to make submission 4 out of 10.", testDialog.getSubmissionNumberText());
  }

  @Test
  public void testSubmissionDialogActions() {
    TestDialog testDialog = createTestDialog();
    Assert.assertEquals("The dialog has OK and Cancel actions", 2, testDialog.getActions().length);
  }

  @Test
  public void testSubmissionDialogValidation() {
    TestDialog testDialog = createTestDialog();

    ValidationInfo validationInfo = testDialog.doValidate();
    Assert.assertNotNull("The validation fails when no group is yet selected", validationInfo);
    Assert.assertEquals("The error message prompts the user to select a group", "Select a group",
        validationInfo.message);

    testDialog.groupComboBox.setSelectedIndex(1);
    Assert.assertNull("The validation passes when a group is selected", testDialog.doValidate());
  }

}
