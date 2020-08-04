package fi.aalto.cs.apluscourses.intellij

import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, Presentation}
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.scala.console.configuration.{ScalaConsoleConfigurationType, ScalaConsoleRunConfiguration, ScalaConsoleRunConfigurationFactory}

trait TestHelperScala {

  def getModuleManager(project: Project): ModuleManager =
    ModuleManager.getInstance(project)

  def createAndAddModule(project: Project, name: String, moduleTypeId: String): Unit = {
    val manager = getModuleManager(project)
    val r: Runnable = () => manager.newModule(name, moduleTypeId)
    WriteCommandAction.runWriteCommandAction(project, r)
  }

  //  this is copied from: https://github.com/JetBrains/intellij-community/blob/5dff73ead7da6e5f4d40a037958c74c9764bdb17/java/compiler/tests/com/intellij/compiler/artifacts/ui/ArtifactEditorActionTestCase.java#L45-L49
  def createActionEvent(action: AnAction) = {
    val presentation = new Presentation
    presentation.copyFrom(action.getTemplatePresentation)
    AnActionEvent.createFromAnAction(action, null, "", DataManager.getInstance.getDataContext(null))
  }

  def getConfiguration(project: Project): ScalaConsoleRunConfiguration = {
    val configurationType = ConfigurationTypeUtil
      .findConfigurationType(classOf[ScalaConsoleConfigurationType])
    new ScalaConsoleRunConfigurationFactory(configurationType)
      .createTemplateConfiguration(project).asInstanceOf[ScalaConsoleRunConfiguration]
  }
}
