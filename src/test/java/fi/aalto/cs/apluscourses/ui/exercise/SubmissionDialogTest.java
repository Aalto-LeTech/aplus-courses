package fi.aalto.cs.apluscourses.ui.exercise;

import static org.hamcrest.Matchers.containsString;

import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.testFramework.LightIdeaTestCase;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionViewModel;
import fi.aalto.cs.apluscourses.ui.base.CheckBox;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

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
                                              @NotNull List<SubmittableFile> submittableFiles,
                                              @NotNull Map<String, Path> filePaths,
                                              int numberOfSubmissions,
                                              int maxNumberOfSubmissions) {
    var info = new SubmissionInfo(Collections.singletonMap("en", submittableFiles));
    var exercise = new Exercise(
        1, exerciseName, "http://www.fi", info, 0, maxNumberOfSubmissions, OptionalLong.empty());
    IntStream.range(0, numberOfSubmissions).forEach(i -> exercise.addSubmissionResult(
        new SubmissionResult(i, 2, 0.0, SubmissionResult.Status.GRADED, exercise)));
    return new SubmissionViewModel(
        exercise,
        availableGroups,
        defaultGroup,
        filePaths,
        "en"
    );
  }

  @NotNull
  private TestDialog createTestDialog() throws IOException {
    Map<String, Path> submittableFilePaths = new HashMap<>();
    for (int i = 0; i < 2; i++) {
      submittableFilePaths.put("file" + i,
              FileUtilRt.createTempFile("testFile", "", true).toPath());
    }

    return new TestDialog(
        createViewModel(
            "Cool Name",
            List.of(
                new Group(123, List.of("Jarkko", "Petteri")),
                new Group(456, List.of("Annika", "Katariina"))
            ),
            new Group(456, List.of("Annika", "Katariina")),
            submittableFilePaths.entrySet().stream()
                .map(x -> new SubmittableFile(x.getKey(), x.getValue().getFileName().toString()))
                .collect(Collectors.toList()),
            submittableFilePaths,
            4,
            10
        )
    );
  }

  @Test
  public void testSubmissionDialogTitle() throws IOException {
    TestDialog testDialog = createTestDialog();
    Assert.assertEquals("The dialog has the correct title", "Submit Assignment",
        testDialog.getTitle());

  }

  @Test
  public void testSubmissionDialogHeader() throws IOException {
    TestDialog testDialog = createTestDialog();
    Assert.assertThat("The header contains the exercise name", testDialog.getHeader(),
        containsString("Cool Name"));
  }

  @Test
  public void testSubmissionDialogGroupSelection() throws IOException {
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
  public void testSubmissionDialogDefaultGroupCheckBox() throws IOException {
    TestDialog testDialog = createTestDialog();
    CheckBox checkBox = testDialog.getDefaultGroupCheckBox();
    Assert.assertTrue(
        "The default group check box is bound to the observable property in the view model",
        checkBox.isSelected()
    );
  }

  @Test
  public void testSubmissionDialogSubmissionCountText() throws IOException {
    TestDialog testDialog = createTestDialog();
    Assert.assertEquals("The submission count text is correct",
        "You are about to make submission 5 out of 10.", testDialog.getSubmissionNumberText());
  }

  @Test
  public void testSubmissionDialogActions() throws IOException {
    TestDialog testDialog = createTestDialog();
    Assert.assertEquals("The dialog has OK and Cancel actions", 2, testDialog.getActions().length);
  }

}
