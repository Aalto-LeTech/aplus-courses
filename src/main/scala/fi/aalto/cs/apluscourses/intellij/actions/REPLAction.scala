package fi.aalto.cs.apluscourses.intellij.actions

import com.intellij.execution.impl.EditConfigurationsDialog
import com.intellij.execution.{RunManager, RunManagerEx, RunnerAndConfigurationSettings}
import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys}
import com.intellij.openapi.module.{ModuleManager, ModuleUtilCore}
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import fi.aalto.cs.apluscourses.ui.REPLModuleSelectorDialog
import org.jetbrains.annotations.Nullable
import org.jetbrains.plugins.scala.console.actions.RunConsoleAction
import org.jetbrains.plugins.scala.console.configuration.ScalaConsoleRunConfiguration

import scala.jdk.CollectionConverters._

class REPLAction extends RunConsoleAction {

  override def actionPerformed(e: AnActionEvent): Unit = {
    println("inside REPLAction: yeah, baby, yeah!")

    customDoRunAction(e)
  }

  protected def checkFileOrFolder(@Nullable fileOrFolder: VirtualFile): Boolean = true

  protected final def customDoRunAction(e: AnActionEvent): Unit = {
    val dataContext = e.getDataContext
    val project = CommonDataKeys.PROJECT.getData(dataContext)
    val targetFileOrFolder = CommonDataKeys.VIRTUAL_FILE.getData(dataContext)

    if (project == null || !checkFileOrFolder(targetFileOrFolder)) return

    val runManagerEx = RunManagerEx.getInstanceEx(project)
    val configurationType = getMyConfigurationType
    val settings = runManagerEx.getConfigurationSettingsList(configurationType).asScala

    val module = ModuleUtilCore.findModuleForFile(targetFileOrFolder, project)
    val moduleDirPath = ModuleUtilCore.getModuleDirPath(module)
    val index = moduleDirPath.indexOf("/.idea/module")

    var setting: RunnerAndConfigurationSettings = null;

    if (settings.nonEmpty) {
      setting = settings.head
    } else if (settings.isEmpty) {
      val factory = configurationType.getConfigurationFactories.apply(0)
      setting = RunManager.getInstance(project).createConfiguration("Scala REPL for module: " + module.getName, factory)
    }

    val dialog = new EditConfigurationsDialog(project)
    if(dialog.showAndGet()){
      val components = dialog.getContentPane.getComponents
    }

    setting.setEditBeforeRun(true)
    val configuration = setting.getConfiguration.asInstanceOf[ScalaConsoleRunConfiguration]
    configuration.setWorkingDirectory(moduleDirPath.substring(0, index))
    configuration.setModule(module)
    configuration.setName("Scala REPL for module: " + module.getName)

    RunConsoleAction.runExisting(setting, runManagerEx, project)
  }
}
