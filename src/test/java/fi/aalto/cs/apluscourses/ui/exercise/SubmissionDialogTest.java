package fi.aalto.cs.apluscourses.ui.exercise;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.testFramework.LightIdeaTestCase;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
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
      super(viewModel, getProject());
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
                                                  @NotNull SubmittableFile[] files,
                                                  int numberOfSubmissions,
                                                  int maxNumberOfSubmissions) {
    SubmissionViewModel viewModel = mock(SubmissionViewModel.class);
    doReturn(exerciseName).when(viewModel).getPresentableExerciseName();
    doReturn(availableGroups).when(viewModel).getAvailableGroups();
    doReturn(files).when(viewModel).getFiles();
    doReturn(numberOfSubmissions).when(viewModel).getCurrentSubmissionNumber();
    doReturn(maxNumberOfSubmissions).when(viewModel).getMaxNumberOfSubmissions();
    return viewModel;
  }

  @NotNull
  private TestDialog createTestDialog() {
    return new TestDialog(
        createMockViewModel(
            "Cool Name",
            Arrays.asList(new Group(123, Arrays.asList("Jarkko", "Petteri"))),
            new SubmittableFile[] { new SubmittableFile("file1"), new SubmittableFile("file2") },
            4,
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

    ComboBoxModel<Group> model = comboBox.getModel();
    Assert.assertEquals("The group selection combo box includes available groups", 123,
        model.getElementAt(0).getId());
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

}
