package fi.aalto.cs.apluscourses.ui.repl;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import fi.aalto.cs.apluscourses.TestHelper;
import fi.aalto.cs.apluscourses.presentation.ReplConfigurationFormModel;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ReplConfigurationFormTest extends BasePlatformTestCase implements TestHelper {

  @Test
  public void testFormCreationWithValidInputWorks() {
    //  given
    ReplConfigurationFormModel model = getDummyReplConfigurationFormModel();
    String workDir = getProject().getProjectFilePath();
    String moduleName = "light_idea_test_case";

    //  when
    ReplConfigurationForm form = new ReplConfigurationForm(model);

    //  then
    assertEquals("Form target module is chosen correctly",
        moduleName, (String) form.getModuleComboBox().getSelectedItem());
    assertEquals("Form working directory is chosen correctly",
        workDir, form.getWorkingDirectoryField().getText());
    assertEquals("Form contains the rights informative message",
        ReplConfigurationForm.INFOLABEL_TEXT, form.getInfoTextLabel().getText());
    assertEquals("Form correctly picks up negated flag for showing the config dialog",
        ReplConfigurationFormModel.showREPLConfigWindow,
        form.getDontShowThisWindowCheckBox().isSelected());
  }

  @Test
  public void testUpdateModel() {
    //  given
    ReplConfigurationFormModel model = getDummyReplConfigurationFormModel();
    ReplConfigurationForm form = new ReplConfigurationForm(model);
    String updatedWorkDir = "updateWorkDirPath";
    form.getWorkingDirectoryField().setText(updatedWorkDir);
    String fakeModuleName = "fakeModuleName";
    form.getModuleComboBox().addItem(fakeModuleName);
    form.getModuleComboBox().setSelectedItem(fakeModuleName);

    //  when
    form.updateModel();

    //  then
    assertEquals("Presentation model's working directory gets updated properly asked to",
        updatedWorkDir, model.getModuleWorkingDirectory());
    assertEquals("Presentation model's target (selected) module names gets properly updated.",
        fakeModuleName, model.getTargetModuleName());
  }

  @NotNull
  private ReplConfigurationFormModel getDummyReplConfigurationFormModel() {
    return getDummyReplConfigurationFormModel(getProject());
  }
}