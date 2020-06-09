package fi.aalto.cs.apluscourses.intellij.actions

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import fi.aalto.cs.apluscourses.intellij.TestHelperScala
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings
import org.junit.Assert._
import org.junit.Test

class ReplActionTest extends BasePlatformTestCase with TestHelperScala {

  @Test
  def testSetConfigurationConditionallyWithDoNotShowReplFlagWorks(): Unit = {
    //  given
    val project = getProject
    val module = getModule
    val configuration = getConfiguration
    val replTitle = s"REPL for ${module.getName}"
    val action = new ReplAction
    val moduleWorkDir = action.getModuleWorkDir(module)

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
  def testSetCustomConfigurationFieldsWithValidInputWorks(): Unit = {
    //  given
    val configuration = getConfiguration
    val module = getModule
    val replTitle = s"REPL for ${module.getName}"
    val action = new ReplAction
    val moduleWorkDir = action.getModuleWorkDir(module)

    //  when
    action.setCustomConfigurationFields(configuration, moduleWorkDir, module)

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
    val replTitle = s"REPL for ${module.getName}"
    val action = new ReplAction

    //  when
    action.setCustomConfigurationFields(configuration, moduleWorkDir, module)

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
    val replTitle = s"REPL for ${module.getName}"
    val action = new ReplAction

    //  when
    action.setCustomConfigurationFields(configuration, moduleWorkDir, module)

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
