package fi.aalto.cs.apluscourses.intellij.actions

import com.intellij.testFramework.HeavyPlatformTestCase
import fi.aalto.cs.apluscourses.intellij.TestHelperScala
import fi.aalto.cs.apluscourses.intellij.utils.ModuleUtils
import org.junit.Assert.{assertEquals, assertSame, assertTrue}
import org.junit.Test

class ReplActionHeavyTest extends HeavyPlatformTestCase with TestHelperScala {

  @Test
  def testGetModuleWorkDirWithValidModuleWorks(): Unit = {
    //  given
    createAndAddModule("fakeModulePath", "fakeModuleId")
    val modules = getModuleManager.getModules
    val configuration = getConfiguration
    val module = modules.apply(1)
    val replTitle = s"REPL for ${module.getName}"
    val action = new ReplAction
    val moduleWorkDir = ModuleUtils.getModuleDirectory(modules.head)

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

  private def getModuleManager = super.getModuleManager(getProject)

  private def createAndAddModule(path: String, id: String): Unit = super.createAndAddModule(getProject, path, id)
}
