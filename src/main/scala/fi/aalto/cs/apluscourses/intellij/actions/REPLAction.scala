package fi.aalto.cs.apluscourses.intellij.actions

import com.intellij.execution.{RunManager, RunManagerEx, RunnerAndConfigurationSettings}
import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys}
import com.intellij.openapi.module.{Module, ModuleUtilCore}
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.{NotNull, Nullable}
import org.jetbrains.plugins.scala.console.actions.RunConsoleAction
import org.jetbrains.plugins.scala.console.configuration.ScalaConsoleRunConfiguration

import collection.JavaConverters._

class REPLAction extends RunConsoleAction {

  override def actionPerformed(e: AnActionEvent): Unit = {
    customDoRunAction(e)
  }

  def checkFileOrFolderIsNotNull(@Nullable fileOrFolder: VirtualFile): Boolean = {
    fileOrFolder != null
  }

  def adjustRunConfigurationSettings(@NotNull module: Module, @NotNull configuration: ScalaConsoleRunConfiguration) = {
    // adjust the configuration with: name, workDir and module
    val moduleDirPath = ModuleUtilCore.getModuleDirPath(module)
    configuration.setWorkingDirectory(moduleDirPath)
    configuration.setModule(module)
    configuration.setName("Scala REPL for module: " + module.getName)
  }

  protected def customDoRunAction(e: AnActionEvent): Unit = {
    val dataContext = e.getDataContext
    val project = CommonDataKeys.PROJECT.getData(dataContext)
    // virtual file is working for both: files and folders
    val targetFileOrFolder = CommonDataKeys.VIRTUAL_FILE.getData(dataContext)

    if (project == null || !checkFileOrFolderIsNotNull(targetFileOrFolder)) return

    val runManagerEx = RunManagerEx.getInstanceEx(project)
    val configurationType = getMyConfigurationType
    val settings = runManagerEx.getConfigurationSettingsList(configurationType).asScala

    // get target module
    val module = ModuleUtilCore.findModuleForFile(targetFileOrFolder, project)

    //choose the configuration to run based on the condition if this a new configuration of not
    def chooseConfigurationSettings:RunnerAndConfigurationSettings = {
      var setting: RunnerAndConfigurationSettings = null
      if (settings.nonEmpty) {
        setting = settings.head
      } else if (settings.isEmpty) {
        val factory = configurationType.getConfigurationFactories.apply(0)
        setting = RunManager.getInstance(project).createConfiguration("Scala REPL for module: " + module.getName, factory)
      }
      setting
    }

    val setting = chooseConfigurationSettings
    val configuration = setting.getConfiguration.asInstanceOf[ScalaConsoleRunConfiguration]

    adjustRunConfigurationSettings(module, configuration)
    RunConsoleAction.runExisting(setting, runManagerEx, project)
  }
}
