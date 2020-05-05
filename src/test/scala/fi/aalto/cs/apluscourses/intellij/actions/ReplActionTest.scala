package fi.aalto.cs.apluscourses.intellij.actions


import com.intellij.mock.MockVirtualFile.{dir, file}
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import fi.aalto.cs.apluscourses.intellij.TestHelperScala
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings
import org.junit.Assert._
import org.junit.Test
import org.mockito.Mockito.{spy, times, verify}

class ReplActionTest extends BasePlatformTestCase with TestHelperScala {

  @Test
  def testSetConfigurationConditionallyWithDoNotShowReplFlagWorks(): Unit = {
    //  given
    val project = getProject
    val module = getModule
    val configuration = getConfiguration
    val replTitle = s"REPL in ${module.getName}"
    val action = new ReplAction
    val moduleWorkDir = action.getModuleWorkDir(module)

    //  only FALSE branch, as TRUE triggers UI
    PluginSettings.setShowReplConfigurationDialog(false.toString);

    //  when
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
    val replTitle = s"REPL in ${module.getName}"
    val action = new ReplAction
    val moduleWorkDir = action.getModuleWorkDir(module)

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
  def testSetCustomConfigurationFieldsWithMixedWorkDirModuleInputWorks(): Unit = {
    //  given
    val configuration = getConfiguration
    val module = getModule
    val moduleWorkDir = "/fakeWorkDir"
    val replTitle = s"REPL in <?>"
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
  def testSetCustomConfigurationFieldsWithMixedModuleWorkDirInputWorks(): Unit = {
    //  given
    val configuration = getConfiguration
    val module = getModule
    val moduleWorkDir = "/tmp/unitTest_setCustomConfigurationFieldsWithMixedModuleWorkDirInputWorks"
    val replTitle = s"REPL in <?>"
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

  private def getConfiguration = super.getConfiguration(getProject)
}
