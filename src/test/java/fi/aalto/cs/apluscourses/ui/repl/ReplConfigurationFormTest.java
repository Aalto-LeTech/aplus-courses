package fi.aalto.cs.apluscourses.ui.repl;

import static org.junit.Assert.assertNotEquals;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.TestHelper;
import fi.aalto.cs.apluscourses.presentation.ReplConfigurationFormModel;
import org.junit.Test;

public class ReplConfigurationFormTest extends TestHelper {

  @Test
  public void testFormCreationWithValidInputWorks() {
    //  given
    String workDir = getProject().getProjectFilePath();
    String moduleName = "light_idea_test_case";
    ReplConfigurationFormModel model = getDummyReplConfigurationFormModel();

    //  when
    ReplConfigurationForm form = new ReplConfigurationForm(model);

    //  then
    assertEquals("Form contains the rights informative message",
        ReplConfigurationForm.INFOLABEL_TEXT, form.getInfoTextLabel().getText());
    assertNotEquals("Form correctly picks up negated flag for showing the config dialog",
        ReplConfigurationFormModel.showREPLConfigWindow,
        form.getDontShowThisWindowCheckBox().isSelected());
    assertEquals("Form working directory is chosen correctly",
        workDir, form.getWorkingDirectoryField().getText());
    assertEquals("Form target module is chosen correctly",
        moduleName, (String) form.getModuleComboBox().getSelectedItem());
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

  private ReplConfigurationFormModel getDummyReplConfigurationFormModel() {
    Project project = getProject();
    String workDir = project.getProjectFilePath();
    String moduleName = "light_idea_test_case";
    makeFirstPluginScalaModule(project);
    return new ReplConfigurationFormModel(project, workDir, moduleName);
  }
}