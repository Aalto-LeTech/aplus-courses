package fi.aalto.cs.apluscourses.intellij.actions

import com.intellij.execution.RunManagerEx
import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys}
import com.intellij.openapi.module.{Module, ModuleManager, ModuleUtilCore}
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import fi.aalto.cs.apluscourses.intellij.utils.CancelReplStartException
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

  override def actionPerformed(@NotNull e: AnActionEvent): Unit = {
    customDoRunAction(e)
  }

  def checkFileOrFolderIsNull(@Nullable fileOrFolder: VirtualFile): Boolean = fileOrFolder == null

  def getModuleWorkDir(@NotNull module: Module): String = {
    //  for project "root" this points to .../.../.idea folder
    ModuleUtilCore.getModuleDirPath(module).replace("/.idea", "")
  }

  def setCustomConfigurationFields(@NotNull configuration: ScalaConsoleRunConfiguration,
                                   @NotNull workDir: String,
                                   @NotNull moduleName: String,
                                   @NotNull module: Module): Unit = {
    configuration.setWorkingDirectory(workDir)
    configuration.setModule(module)
    if (getModuleWorkDir(module).equals(workDir)) {
      configuration.setName("REPL in " + moduleName)
    } else {
      configuration.setName("REPL in <?>")
    }
  }

  def setConfigurationConditionally(@NotNull project: Project,
                                    @NotNull module: Module,
                                    @NotNull configuration: ScalaConsoleRunConfiguration): Unit = {
    if (ReplConfigurationFormModel.showREPLConfigWindow) {
      val configModel = new ReplConfigurationFormModel(project,
        getModuleWorkDir(module), module.getName)

      createAndShowReplConfigurationDialog(configModel)

      if (!configModel.isStartRepl) {
        throw new CancelReplStartException
      }

      val changedModuleName = configModel.getTargetModuleName
      val changedModule = ModuleManager.getInstance(project).findModuleByName(changedModuleName)
      val changedWorkDir = configModel.getModuleWorkingDirectory
      setCustomConfigurationFields(configuration, changedWorkDir, changedModuleName, changedModule)
    } else {
      setCustomConfigurationFields(configuration, getModuleWorkDir(module), module.getName, module)
    }
  }

  private def createAndShowReplConfigurationDialog(@NotNull configModel: ReplConfigurationFormModel):
  ReplConfigurationDialog = {
    val configForm = new ReplConfigurationForm(configModel)
    val configDialog = new ReplConfigurationDialog
    configDialog.setReplConfigurationForm(configForm)
    configDialog.setVisible(true)
    configDialog
  }

  /**
   * Method that sets working directory and module of the REPL it was started from. Works for REPL
   * triggered on files or folders within the module scope.
   *
   * @param e an [[AnActionEvent]] with payload.
   */
  def customDoRunAction(@NotNull e: AnActionEvent): Unit = {
    val dataContext = e.getDataContext
    val project = CommonDataKeys.PROJECT.getData(dataContext)
    //  virtual file is working for both: files and folders
    val targetFileOrFolder = CommonDataKeys.VIRTUAL_FILE.getData(dataContext)

    if (project == null || checkFileOrFolderIsNull(targetFileOrFolder)) return // scalastyle:ignore

    //  get target module & workDir
    val module = ModuleUtilCore.findModuleForFile(targetFileOrFolder, project)

    val runManagerEx = RunManagerEx.getInstanceEx(project)
    val configurationType = getMyConfigurationType
    val settings = runManagerEx.getConfigurationSettingsList(configurationType).asScala

    //  choose the configuration to run based on the condition if this a new configuration of not
    val setting = settings.headOption.getOrElse {
      val factory = configurationType.getConfigurationFactories.head
      runManagerEx.createConfiguration(s"REPL in ${module.getName}", factory)
    }

    val configuration = setting.getConfiguration.asInstanceOf[ScalaConsoleRunConfiguration]
    try {
      setConfigurationConditionally(project, module, configuration)
    } catch {
      case _: CancelReplStartException => return // scalastyle:ignore
    }

    RunConsoleAction.runExisting(setting, runManagerEx, project)
  }
}
