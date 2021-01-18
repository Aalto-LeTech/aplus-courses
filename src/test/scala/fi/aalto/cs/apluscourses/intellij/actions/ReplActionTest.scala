package fi.aalto.cs.apluscourses.intellij.actions

import com.intellij.openapi.actionSystem.{AnActionEvent, DataContext}
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import fi.aalto.cs.apluscourses.intellij.TestHelperScala
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings
import fi.aalto.cs.apluscourses.intellij.utils.ModuleUtils
import org.junit.Assert._
import org.junit.{Ignore, Test}
import org.mockito.Mockito.{mock, when}

@Ignore
class ReplActionTest extends BasePlatformTestCase with TestHelperScala {

  @Test
  def testDoesNothingIfProjectNull(): Unit = {
    val anActionEvent = mock(classOf[AnActionEvent])
    when(anActionEvent.getDataContext).thenReturn(new DataContext {
      override def getData(dataId: String) = null
    })
    // This would throw an exception if the method wouldn't return early when the project is null
    (new ReplAction).actionPerformed(anActionEvent)
  }

  @Test
  def testSetConfigurationConditionallyWithDoNotShowReplFlagWorks(): Unit = {
    //  given
    val project = getProject
    val module = getModule
    val configuration = getConfiguration
    val replTitle = s"REPL for ${module.getName}"
    val action = new ReplAction
    val moduleWorkDir = ModuleUtils.getModuleDirectory(module)

    //  only FALSE branch, as TRUE triggers UI
    PluginSettings.getInstance.setShowReplConfigurationDialog(false);

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
  def testSetConfigurationFieldsWithValidInputWorks(): Unit = {
    //  given
    val configuration = getConfiguration
    val module = getModule
    val replTitle = s"REPL for ${module.getName}"
    val action = new ReplAction
    val moduleWorkDir = ModuleUtils.getModuleDirectory(module)

    //  when
    action.setConfigurationFields(configuration, moduleWorkDir, module)

    //  then
    assertTrue("REPL's (configuration) working directory has been properly set",
      configuration.getWorkingDirectory.contains(moduleWorkDir))
    assertEquals("REPL's (configuration) title has been properly set",
      replTitle, configuration.getName)
    assertSame("REPL's (configuration) working Module has been properly set",
      module, configuration.getModules.head)
  }

  @Test
  def testSetConfigurationFieldsWithMixedWorkDirModuleInputWorks(): Unit = {
    //  given
    val configuration = getConfiguration
    val module = getModule
    val moduleWorkDir = "/fakeWorkDir"
    val replTitle = s"REPL for ${module.getName}"
    val action = new ReplAction

    //  when
    action.setConfigurationFields(configuration, moduleWorkDir, module)

    //  then
    assertTrue("REPL's (configuration) working directory has been properly set",
      configuration.getWorkingDirectory.contains(moduleWorkDir))
    assertEquals("REPL's (configuration) title has been properly set",
      replTitle, configuration.getName)
    assertSame("REPL's (configuration) working Module has been properly set",
      module, configuration.getModules.head)
  }

  @Test
  def testSetConfigurationFieldsWithMixedModuleWorkDirInputWorks(): Unit = {
    //  given
    val configuration = getConfiguration
    val module = getModule
    val moduleWorkDir = "/tmp/unitTest_setCustomConfigurationFieldsWithMixedModuleWorkDirInputWorks"
    val replTitle = s"REPL for ${module.getName}"
    val action = new ReplAction

    //  when
    action.setConfigurationFields(configuration, moduleWorkDir, module)

    //  then
    assertTrue("REPL's (configuration) working directory has been properly set",
      configuration.getWorkingDirectory.contains(moduleWorkDir))
    assertEquals("REPL's (configuration) title has been properly set",
      replTitle, configuration.getName)
    assertSame("REPL's (configuration) working Module has been properly set",
      module, configuration.getModules.head)
  }

  private def getConfiguration = super.getConfiguration(getProject)
}
