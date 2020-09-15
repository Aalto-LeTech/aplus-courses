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
    val project = mock(classOf[Project])
    when(project.isDefault).thenReturn(true)
    when(module.getProject).thenReturn(project)
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

}
