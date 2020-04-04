import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, Presentation}
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import fi.aalto.cs.apluscourses.intellij.actions.REPLAction
import org.jetbrains.plugins.scala.console.configuration.{ScalaConsoleConfigurationType, ScalaConsoleRunConfiguration, ScalaConsoleRunConfigurationFactory}
import org.junit.Assert._
import org.junit.Test
import org.mockito.Mockito.{spy, times, verify}

class REPLActionTest extends BasePlatformTestCase {

  @Test
  def testAdjustRunConfigurationSettingsWithValidInputWorks(): Unit = {
    val project = getProject
    val module = getModule
    val factory = new ScalaConsoleRunConfigurationFactory(new ScalaConsoleConfigurationType)
    val configuration = factory.createTemplateConfiguration(project).asInstanceOf[ScalaConsoleRunConfiguration]
    configuration.setWorkingDirectory("fakeDirectoryName")
    val action = new REPLAction

    action.adjustRunConfigurationSettings(module, configuration)

    assertEquals("Customized title of the Scala REPL contains the name of the module started from", "Scala REPL for module: light_idea_test_case", configuration.getName)
    assertTrue("Customized working directory is set to be a subFolder of '/tmp/'", configuration.getWorkingDirectory.contains("/tmp/unitTest_"))
    assertTrue("Configuration is triggered with the module is has been started from", configuration.getAllModules.contains(module))
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testCustomDoRunActionIsTriggered(): Unit = {
    //given
    val action = new REPLAction
    val spyAction = spy(action)
    val actionEvent = createActionEvent(action)

    //when
    spyAction.actionPerformed(actionEvent)

    //then
    verify(spyAction, times(1)).customDoRunAction(actionEvent)
  }

  // this is copied from: https://github.com/JetBrains/intellij-community/blob/5dff73ead7da6e5f4d40a037958c74c9764bdb17/java/compiler/tests/com/intellij/compiler/artifacts/ui/ArtifactEditorActionTestCase.java#L45-L49
  private def createActionEvent(action: AnAction) = {
    val presentation = new Presentation
    presentation.copyFrom(action.getTemplatePresentation)
    AnActionEvent.createFromAnAction(action, null, "", DataManager.getInstance.getDataContext(null))
  }
}
