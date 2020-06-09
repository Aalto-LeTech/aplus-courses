package fi.aalto.cs.apluscourses.intellij.actions

import java.util

import com.intellij.execution.RunManagerEx
import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys, DataContext}
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.{Module, ModuleManager, ModuleUtilCore}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.roots.{LibraryOrderEntry, ModuleRootManager}
import com.intellij.openapi.util.io.FileUtilRt.toSystemIndependentName
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Processor
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings
import fi.aalto.cs.apluscourses.intellij.utils.ListDependenciesPolicy
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

  def customDoRunAction(@NotNull e: AnActionEvent): Unit = {
    val dataContext = e.getDataContext
    val project = CommonDataKeys.PROJECT.getData(dataContext)
    if (project == null) return // scalastyle:ignore

    val runManagerEx = RunManagerEx.getInstanceEx(project)
    val configurationType = getMyConfigurationType
    val settings = runManagerEx.getConfigurationSettingsList(configurationType).asScala

    /*
     * The "priority order" is as follows:
     *   1. If a file is open in the editor and it belongs to a module that has Scala SDK as a
     *      library dependency, start the REPL for the module of the file.
     *   2. Otherwise if a module (or file inside a module) is selected in the project menu on the
     *      left and the module has Scala SDK as a library dependency, start the REPL for that
     *      module.
     *   3. Otherwise start a project level REPL
     *
     * Checking that the module has Scala SDK as a dependency is done to avoid the "no Scala facet
     * configured for module" error.
     */
    val selectedModule = getScalaModuleOfEditorFile(project, dataContext)
      .orElse(getScalaModuleOfSelectedFile(project, dataContext))

    // Choose the configuration to run based on the condition if this a new configuration of not
    val setting = settings.headOption.getOrElse {
      val factory = configurationType.getConfigurationFactories.head
      val configurationName = selectedModule
        .map(module => s"REPL for ${module.getName}")
        .getOrElse("Scala REPL")
      runManagerEx.createConfiguration(configurationName, factory)
    }

    selectedModule match {
      case Some(module) =>
        val configuration = setting.getConfiguration.asInstanceOf[ScalaConsoleRunConfiguration]
        if (setConfigurationConditionally(project, module, configuration)) {
          RunConsoleAction.runExisting(setting, runManagerEx, project)
        }
      case None =>
        val configuration = setting.getConfiguration.asInstanceOf[ScalaConsoleRunConfiguration]
        configuration.setName("Scala REPL")
        RunConsoleAction.runExisting(setting, runManagerEx, project)
    }
  }

  def getModuleWorkDir(@NotNull module: Module): String = {
    toSystemIndependentName(ModuleUtilCore.getModuleDirPath(module))
  }

  def setCustomConfigurationFields(@NotNull configuration: ScalaConsoleRunConfiguration,
                                   @NotNull workDir: String,
                                   @NotNull module: Module): Unit = {
    configuration.setWorkingDirectory(workDir)
    configuration.setModule(module)
    configuration.setName(s"REPL for ${module.getName}")
  }

  /**
   * Sets configuration fields for the given configuration and returns true. Returns false if
   * the REPL start is cancelled (i.e. user selects "Cancel" in the REPL configuration dialog).
   */
  def setConfigurationConditionally(@NotNull project: Project,
                                    @NotNull module: Module,
                                    @NotNull configuration: ScalaConsoleRunConfiguration): Boolean = {
    if (PluginSettings.getInstance.shouldShowReplConfigurationDialog) {
      val configModel = new ReplConfigurationFormModel(project, getModuleWorkDir(module), module.getName)
      showReplConfigurationDialog(configModel)

      if (!configModel.isStartRepl) {
        false
      } else {
        val changedModuleName = configModel.getTargetModuleName
        val changedModule = ModuleManager.getInstance(project).findModuleByName(changedModuleName)
        val changedWorkDir = toSystemIndependentName(configModel.getModuleWorkingDirectory)
        setCustomConfigurationFields(configuration, changedWorkDir, changedModule)
        true
      }
    } else {
      setCustomConfigurationFields(configuration, getModuleWorkDir(module), module)
      true
    }
  }

  private def showReplConfigurationDialog(@NotNull configModel: ReplConfigurationFormModel) = {
    val configForm = new ReplConfigurationForm(configModel)
    val configDialog = new ReplConfigurationDialog
    configDialog.setReplConfigurationForm(configForm)
    configDialog.setVisible(true)
  }

  private def getScalaModuleOfEditorFile(@NotNull project: Project,
                                         @NotNull dataContext: DataContext): Option[Module] = {
    val module = for {
      editor <- Option(CommonDataKeys.EDITOR.getData(dataContext))
      openFile <- Option(FileDocumentManager.getInstance.getFile(editor.getDocument))
    } yield ModuleUtilCore.findModuleForFile(openFile, project)
    module.filter(hasScalaSdkLibrary)
  }

  private def getScalaModuleOfSelectedFile(@NotNull project: Project,
                                           @NotNull dataContext: DataContext): Option[Module] = {
    Option(CommonDataKeys.VIRTUAL_FILE.getData(dataContext))
      .map(file => ModuleUtilCore.findModuleForFile(file, project))
      .filter(hasScalaSdkLibrary)
  }

  private def hasScalaSdkLibrary(@NotNull module: Module): Boolean = {
    var foundScalaSdk = false
    ModuleRootManager.getInstance(module)
      .orderEntries()
      .forEachLibrary((library: Library) => {
        foundScalaSdk = foundScalaSdk || Option(library.getName).exists(_.contains("scala-sdk-"))
        !foundScalaSdk
      })
    foundScalaSdk
  }

}
