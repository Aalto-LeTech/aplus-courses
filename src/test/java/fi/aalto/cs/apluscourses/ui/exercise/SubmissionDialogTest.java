package fi.aalto.cs.apluscourses.ui.exercise;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.testFramework.LightIdeaTestCase;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.SubmissionHistory;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionViewModel;
import fi.aalto.cs.apluscourses.ui.base.CheckBox;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    public CheckBox getDefaultGroupCheckBox() {
      return super.defaultGroupCheckBox;
    }

    public String getSubmissionNumberText() {
      return super.submissionCount.getText();
    }

    public Action[] getActions() {
      return super.createActions();
    }

  }

  @NotNull
  private SubmissionViewModel createViewModel(@NotNull String exerciseName,
                                              @NotNull List<Group> availableGroups,
                                              @Nullable Group defaultGroup,
                                              @NotNull List<SubmittableFile> files,
                                              int numberOfSubmissions,
                                              int maxNumberOfSubmissions) {
    return new SubmissionViewModel(
        new Exercise(1, exerciseName, "http://www.fi", 0, 0, maxNumberOfSubmissions),
        new SubmissionInfo(maxNumberOfSubmissions, Collections.singletonMap("en", files)),
        new SubmissionHistory(numberOfSubmissions),
        availableGroups,
        defaultGroup,
        Collections.emptyMap(),
        "en"
    );
  }

  @NotNull
  private TestDialog createTestDialog() {
    return new TestDialog(
        createViewModel(
            "Cool Name",
            Arrays.asList(
                new Group(123, Arrays.asList("Jarkko", "Petteri")),
                new Group(456, Arrays.asList("Annika", "Katariina"))
            ),
            new Group(456, Arrays.asList("Annika", "Katariina")),
            Arrays.asList(
                new SubmittableFile("file1", "main.c"),
                new SubmittableFile("file2", "main.h")
            ),
            4,
            10
        )
    );
  }

  @Test
  public void testSubmissionDialogTitle() {
    TestDialog testDialog = createTestDialog();
    Assert.assertEquals("The dialog has the correct title", "Submit Assignment",
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
    Assert.assertEquals("The group selection combo box includes available groups", 456,
        model.getElementAt(1).getId());
    Assert.assertEquals("The selected item is the default group", 456,
        ((Group) model.getSelectedItem()).getId());
  }

  @Test
  public void testSubmissionDialogDefaultGroupCheckBox() {
    TestDialog testDialog = createTestDialog();
    CheckBox checkBox = testDialog.getDefaultGroupCheckBox();
    Assert.assertTrue(
        "The default group check box is bound to the observable property in the view model",
        checkBox.isSelected()
    );
  }

  @Test
  public void testSubmissionDialogSubmissionCountText() {
    TestDialog testDialog = createTestDialog();
    Assert.assertEquals("The submission count text is correct",
        "You are about to make submission 5 out of 10.", testDialog.getSubmissionNumberText());
  }

  @Test
  public void testSubmissionDialogActions() {
    TestDialog testDialog = createTestDialog();
    Assert.assertEquals("The dialog has OK and Cancel actions", 2, testDialog.getActions().length);
  }

}
