package fi.aalto.cs.apluscourses.intellij.actions

import com.intellij.execution.{RunManager, RunManagerEx}
import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys}
import com.intellij.openapi.module.{Module, ModuleUtilCore}
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.{NotNull, Nullable}
import org.jetbrains.plugins.scala.console.actions.RunConsoleAction
import org.jetbrains.plugins.scala.console.configuration.ScalaConsoleRunConfiguration

import scala.collection.JavaConverters._

/**
 * Custom class that adjusts Scala Plugin's own RunConsoleAction with A+ requirements.
 */
class REPLAction extends RunConsoleAction {

  override def actionPerformed(e: AnActionEvent): Unit = {
    customDoRunAction(e)
  }

  def checkFileOrFolderIsNull(@Nullable fileOrFolder: VirtualFile): Boolean = fileOrFolder == null

  def adjustRunConfigurationSettings(@NotNull module: Module, @NotNull configuration: ScalaConsoleRunConfiguration): Unit = {
    // adjust the configuration with: name, workDir and module
    val moduleDirPath = ModuleUtilCore.getModuleDirPath(module)
    configuration.setWorkingDirectory(moduleDirPath)
    configuration.setModule(module)
    configuration.setName("Scala REPL for module: " + module.getName)
  }

  /**
   * Method that sets working directory and module of the REPL it was started from. Works for REPL
   * triggered on files or folders within the module scope.
   *
   * @param e an [[AnActionEvent]] with payload.
   */
  def customDoRunAction(e: AnActionEvent): Unit = {
    val dataContext = e.getDataContext
    val project = CommonDataKeys.PROJECT.getData(dataContext)
    // virtual file is working for both: files and folders
    val targetFileOrFolder = CommonDataKeys.VIRTUAL_FILE.getData(dataContext)

    if (project == null || checkFileOrFolderIsNull(targetFileOrFolder)) return

    val runManagerEx = RunManagerEx.getInstanceEx(project)
    val configurationType = getMyConfigurationType
    val settings = runManagerEx.getConfigurationSettingsList(configurationType).asScala

    // get target module
    val module = ModuleUtilCore.findModuleForFile(targetFileOrFolder, project)

    //choose the configuration to run based on the condition if this a new configuration of not
    val setting = settings.headOption.getOrElse {
      val factory = configurationType.getConfigurationFactories.apply(0)
      RunManager.getInstance(project).createConfiguration(s"Scala REPL for module: ${module.getName}", factory)
    }

    val configuration = setting.getConfiguration.asInstanceOf[ScalaConsoleRunConfiguration]

    adjustRunConfigurationSettings(module, configuration)
    RunConsoleAction.runExisting(setting, runManagerEx, project)
  }
}
