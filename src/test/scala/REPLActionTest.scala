import com.intellij.mock.MockVirtualFile.{dir, file}
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import fi.aalto.cs.apluscourses.intellij.actions.REPLAction
import org.jetbrains.plugins.scala.console.configuration.{ScalaConsoleConfigurationType, ScalaConsoleRunConfiguration, ScalaConsoleRunConfigurationFactory}
import org.junit.Assert._
import org.junit.Test


class REPLActionTest extends BasePlatformTestCase {

  @Test
  def testCheckFileOrFolderWithCorrectFileInputWorks(): Unit = {
    val nonNullFileOrFolder = dir("directory")
    val action = new REPLAction

    assertTrue("addMessage", action.checkFileOrFolderIsNotNull(nonNullFileOrFolder))
  }

  @Test
  def testCheckFileOrFolderWithCorrectFolderInputWorks(): Unit = {
    val nonNullFileOrFolder = file("file")
    val action = new REPLAction

    assertTrue("addMessage", action.checkFileOrFolderIsNotNull(nonNullFileOrFolder))
  }

  @Test
  def testCheckFileOrFolderWithEmptyInputFails(): Unit = {
    val nonNullFileOrFolder = null
    val action = new REPLAction

    assertFalse("addMessage", action.checkFileOrFolderIsNotNull(nonNullFileOrFolder))
  }

  @Test
  def testAdjustRunConfigurationSettingsWithValidInputWorks(): Unit = {
    val project = getProject
    val module = getModule
    val factory = new ScalaConsoleRunConfigurationFactory(new ScalaConsoleConfigurationType)
    val configuration = factory.createTemplateConfiguration(project).asInstanceOf[ScalaConsoleRunConfiguration]
    val action = new REPLAction

    action.adjustRunConfigurationSettings(module, configuration)

    assertEquals("addMessage", "Scala REPL for module: light_idea_test_case", configuration.getName)
    assertTrue("addMessage", configuration.getWorkingDirectory.contains("/tmp/unitTest_adjustRunConfigurationSettingsWithValidInputWorks"))
    assertTrue("addMessage", configuration.getAllModules.contains(module))
  }
}
