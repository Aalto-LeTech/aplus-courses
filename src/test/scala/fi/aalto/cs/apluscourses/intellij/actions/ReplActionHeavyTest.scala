package fi.aalto.cs.apluscourses.intellij.actions

import com.intellij.testFramework.HeavyPlatformTestCase
import fi.aalto.cs.apluscourses.intellij.TestHelperScala
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
    val replTitle = s"REPL in <?>"
    val action = new ReplAction
    val moduleWorkDir = action.getModuleWorkDir(modules.apply(0))

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

  def getConfiguration = super.getConfiguration(getProject)

  def getModuleManager = super.getModuleManager(getProject)

  def createAndAddModule(path: String, id: String): Unit = super.createAndAddModule(getProject, path, id)
}
