
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.ide.DataManager
import com.intellij.mock.MockVirtualFile.{dir, file}
import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, Presentation}
import fi.aalto.cs.apluscourses.TestHelper
import fi.aalto.cs.apluscourses.intellij.actions.ReplAction
import fi.aalto.cs.apluscourses.presentation.ReplConfigurationFormModel
import org.jetbrains.plugins.scala.console.configuration.{ScalaConsoleConfigurationType, ScalaConsoleRunConfiguration, ScalaConsoleRunConfigurationFactory}
import org.junit.Assert._
import org.junit.Test
import org.mockito.Mockito.{spy, times, verify}

class ReplActionTest extends TestHelper {

  @Test
  def testSetCustomConfigurationFieldsWithDoNotShowReplFlagWorks(): Unit = {
    val project = getProject
    val module = getModule
    val configuration = getConfiguration
    val replTitle = s"Scala REPL for module: ${module.getName}"
    val action = new ReplAction
    val moduleWorkDir = action.getModuleWorkDir(module)

    //  only FALSE branch, as TRUE triggers UI
    ReplConfigurationFormModel.showREPLConfigWindow = false

    action.setConfigurationConditionally(project, module, configuration)

    //  then
    assertTrue("REPL's (configuration) working directory has been properly set",
      configuration.getWorkingDirectory.contains(moduleWorkDir))
    assertEquals("REPL's (configuration) title has been properly set",
      replTitle, configuration.getName)
    assertSame("REPL's (configuration) working Module has been properly set",
      module, configuration.getModules.head)
  }

  @Test
  def testSetCustomConfigurationFieldsWithValidInputWorks(): Unit = {
    //  given
    val configuration = getConfiguration
    val module = getModule
    val moduleWorkDir = "/tmp/unitTest_adjustRunConfigurationSettings"
    val replTitle = s"Scala REPL for module: ${module.getName}"
    val action = new ReplAction

    //  when
    action.setCustomConfigurationFields(configuration, moduleWorkDir, module.getName, module)

    //  then
    assertTrue("REPL's (configuration) working directory has been properly set",
      configuration.getWorkingDirectory.contains(moduleWorkDir))
    assertEquals("REPL's (configuration) title has been properly set",
      replTitle, configuration.getName)
    assertSame("REPL's (configuration) working Module has been properly set",
      module, configuration.getModules.head)
  }

  @Test
  def testGetModuleWorkDirWithValidModuleWorks(): Unit = {
    val modulePathPart = "/tmp/unitTest_getModuleWorkDirWithValidModuleWorks"
    val module = getModule
    val action = new ReplAction

    assertTrue("Correctly stripped path is returned",
      action.getModuleWorkDir(module).contains(modulePathPart))
  }

  @Test
  def testCheckFileOrFolderWithCorrectFileInputWorks(): Unit = {
    val nonNullFileOrFolder = dir("directory")
    val action = new ReplAction

    assertFalse("Returns 'true' if the given folder is 'null'",
      action.checkFileOrFolderIsNull(nonNullFileOrFolder))
  }

  @Test
  def testCheckFileOrFolderWithCorrectFolderInputWorks(): Unit = {
    val nonNullFileOrFolder = file("file")
    val action = new ReplAction

    assertFalse("Returns 'true' if the given file is 'null'",
      action.checkFileOrFolderIsNull(nonNullFileOrFolder))
  }

  @Test
  def testCheckFileOrFolderWithEmptyInputFails(): Unit = {
    val nonNullFileOrFolder = null
    val action = new ReplAction

    assertTrue("Returns 'false' if the given file/folder is not 'null'",
      action.checkFileOrFolderIsNull(nonNullFileOrFolder))
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testCustomDoRunActionIsTriggered(): Unit = {
    //  given
    val action = new ReplAction
    val spyAction = spy(action)
    val actionEvent = createActionEvent(action)

    //  when
    spyAction.actionPerformed(actionEvent)

    //  then
    verify(spyAction, times(1)).customDoRunAction(actionEvent)
  }

  //  this is copied from: https://github.com/JetBrains/intellij-community/blob/5dff73ead7da6e5f4d40a037958c74c9764bdb17/java/compiler/tests/com/intellij/compiler/artifacts/ui/ArtifactEditorActionTestCase.java#L45-L49
  private def createActionEvent(action: AnAction) = {
    val presentation = new Presentation
    presentation.copyFrom(action.getTemplatePresentation)
    AnActionEvent.createFromAnAction(action, null, "", DataManager.getInstance.getDataContext(null))
  }

  private def getConfiguration: ScalaConsoleRunConfiguration = {
    val project = getProject
    val configurationType = ConfigurationTypeUtil
      .findConfigurationType(classOf[ScalaConsoleConfigurationType])
    new ScalaConsoleRunConfigurationFactory(configurationType)
      .createTemplateConfiguration(project).asInstanceOf[ScalaConsoleRunConfiguration]
  }
}
