package fi.aalto.cs.apluscourses.intellij.actions

import com.intellij.execution.RunManagerEx
import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys}
import com.intellij.openapi.module.{Module, ModuleManager, ModuleUtilCore}
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import fi.aalto.cs.apluscourses.presentation.ReplConfigurationFormModel
import fi.aalto.cs.apluscourses.ui.repl.{ReplConfigurationDialog, ReplConfigurationForm}
import org.jetbrains.annotations.{NotNull, Nullable}
import org.jetbrains.plugins.scala.console.actions.RunConsoleAction
import org.jetbrains.plugins.scala.console.configuration.ScalaConsoleRunConfiguration

import scala.collection.JavaConverters._

/**
 * Custom class that adjusts Scala Plugin's own RunConsoleAction with A+ requirements.
 */
class ReplAction extends RunConsoleAction {

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

    // get target module & workDir
    val module = ModuleUtilCore.findModuleForFile(targetFileOrFolder, project)

    val runManagerEx = RunManagerEx.getInstanceEx(project)
    val configurationType = getMyConfigurationType
    val settings = runManagerEx.getConfigurationSettingsList(configurationType).asScala

    //choose the configuration to run based on the condition if this a new configuration of not
    val setting = settings.headOption.getOrElse {
      val factory = configurationType.getConfigurationFactories.apply(0)
      //todo research RunManager vs. RunManagerEx
      runManagerEx.createConfiguration(s"Scala REPL for module: ${module.getName}", factory)
    }

    val configuration = setting.getConfiguration.asInstanceOf[ScalaConsoleRunConfiguration]
    setConfigurationConditionally(project, module, configuration)

    RunConsoleAction.runExisting(setting, runManagerEx, project)
  }

  def getModuleWorkDir(module: Module) = {
    // for project "root" this points to .../.../.idea folder
    ModuleUtilCore.getModuleDirPath(module).replace("/.idea", "")
  }

  def setConfigurationConditionally(project: Project,
                                    module: Module,
                                    configuration: ScalaConsoleRunConfiguration) = {

    val workDir = getModuleWorkDir(module)
    val moduleName = module.getName

    if (ReplConfigurationFormModel.showREPLConfigWindow) {
      val configModel = new ReplConfigurationFormModel(project, workDir, moduleName)
      createAndShowReplConfigurationDialog(configModel)

      val mName = configModel.getTargetModuleName
      val mod = ModuleManager.getInstance(project).findModuleByName(mName)
      setCustomConfigurationFields(configuration, configModel.getModuleWorkingDirectory, mName, mod)
    } else {
      setCustomConfigurationFields(configuration, workDir, module.getName, module)
    }
  }

  def setCustomConfigurationFields(configuration: ScalaConsoleRunConfiguration,
                                   workDir: String,
                                   mName: String,
                                   mod: Module) = {
    configuration.setWorkingDirectory(workDir)
    configuration.setModule(mod)
    configuration.setName("Scala REPL for module: " + mName)
  }

  def createAndShowReplConfigurationDialog(configModel: ReplConfigurationFormModel): ReplConfigurationDialog = {
    val configForm = new ReplConfigurationForm(configModel)
    val configDialog = new ReplConfigurationDialog
    configDialog.setReplConfigurationForm(configForm)
    configDialog.setVisible(true)
    configDialog
  }
}
