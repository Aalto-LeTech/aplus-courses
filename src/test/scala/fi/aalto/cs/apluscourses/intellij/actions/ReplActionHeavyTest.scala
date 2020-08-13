package fi.aalto.cs.apluscourses.intellij.actions

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.testFramework.HeavyPlatformTestCase
import fi.aalto.cs.apluscourses.intellij.TestHelperScala
import org.junit.Assert.{assertEquals, assertSame, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}

class ReplActionHeavyTest extends HeavyPlatformTestCase with TestHelperScala {

  @Test
  def testGetModuleWorkDirWithValidModuleWorks(): Unit = {
    //  given
    val configuration = getConfiguration
    val module = mock(classOf[Module])
    when(module.getProject).thenReturn(mock(classOf[Project]))
    when(module.getModuleFilePath).thenReturn("directory/module.iml")
    when(module.getName).thenReturn("mock module")

    val replTitle = s"REPL for mock module"
    val action = new ReplAction
    val moduleWorkDir = "directory"

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
