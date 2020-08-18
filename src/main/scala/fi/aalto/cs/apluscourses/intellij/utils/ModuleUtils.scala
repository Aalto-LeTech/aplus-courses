package fi.aalto.cs.apluscourses.intellij.utils

import collection.JavaConverters.asJavaCollection
import com.intellij.openapi.actionSystem.{CommonDataKeys, DataContext}
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.module.{Module, ModuleUtilCore}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.OrderEnumerator
import com.intellij.openapi.util.io.FileUtilRt
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings
import fi.aalto.cs.apluscourses.utils.PluginResourceBundle.{getAndReplaceText, getText}
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Paths

import org.apache.commons.io.FileUtils
import org.jetbrains.annotations.NotNull
import org.slf4j.LoggerFactory

object ModuleUtils {

  val Logger = LoggerFactory.getLogger(ModuleUtils.getClass)

  def getModuleDirectory(@NotNull module: Module): String =
    FileUtilRt.toSystemIndependentName(ModuleUtilCore.getModuleDirPath(module))

  def getModuleOfEditorFile(@NotNull project: Project,
                            @NotNull dataContext: DataContext): Option[Module] = for {
    editor <- Option(CommonDataKeys.EDITOR.getData(dataContext))
    openFile <- Option(FileDocumentManager.getInstance.getFile(editor.getDocument))
  } yield ModuleUtilCore.findModuleForFile(openFile, project)

  def getModuleOfSelectedFile(@NotNull project: Project,
                              @NotNull dataContext: DataContext): Option[Module] =
    Option(CommonDataKeys.VIRTUAL_FILE.getData(dataContext))
      .map(file => ModuleUtilCore.findModuleForFile(file, project))

  def nonEmpty(enumerator: OrderEnumerator): Boolean = {
    var nonEmpty = false
    enumerator.forEach { _ =>
      nonEmpty = true
      false
    }
    nonEmpty
  }

  // O1_SPECIFIC
  def naiveValidate(@NotNull command: String): Boolean =
    command.matches("import\\so1\\.[a-z]*(\\_|\\._)$")

  def clearCommands(@NotNull imports: Array[String]): Array[String] =
    imports
      .clone
      .map(_.replace("import ", ""))
      .map(_.replace("._", ""))

  def getCommandsText(@NotNull imports: Array[String]): String =
    imports.length match {
      case 0 => ""
      case 1 => getAndReplaceText("ui.repl.console.welcome.autoImport.single.message", imports.head)
      case _ => getAndReplaceText("ui.repl.console.welcome.autoImport.multiple.message", imports.mkString(", "))
    }

  def getUpdatedText(@NotNull module: Module,
                     @NotNull commands: Array[String],
                     @NotNull originalText: String): String = {
    val runConsoleShortCut = getPrettyKeyMapString("Scala.RunConsole")
    val executeConsoleShortCut = getPrettyKeyMapString("ScalaConsole.Execute")
    val reRunShortCut = getPrettyKeyMapString("Rerun")
    val editorUpShortCut = getPrettyKeyMapString("EditorUp")
    val editorDownShortCut = getPrettyKeyMapString("EditorDown")

    val commonText = getAndReplaceText("ui.repl.console.welcome.commonText",
      executeConsoleShortCut, editorUpShortCut, editorDownShortCut, reRunShortCut)

    if (isTopLevelModule(module)) {
      getAndReplaceText("ui.repl.console.welcome.noModuleText",
        commonText, originalText, runConsoleShortCut)
    } else {
      val validCommands = commands.filter(command => naiveValidate(command))
      val clearedCommands = clearCommands(validCommands)
      val commandsText = getCommandsText(clearedCommands)

      getAndReplaceText("ui.repl.console.welcome.fullText",
        module.getName, commandsText, commonText, originalText)
    }
  }

  @NotNull
  def getPrettyKeyMapString(@NotNull actionId: String): String = {
    val shortCuts = KeymapManager
      .getInstance
      .getActiveKeymap
      .getShortcuts(actionId)

    if (shortCuts.nonEmpty) {
      shortCuts
        .head
        .toString
        .replace("[", "")
        .replace("]", "")
        .split(" ")
        .filter(_ != "pressed")
        .map(_.toLowerCase)
        .map(_.capitalize)
        .mkString("+")
    } else {
      getText("ui.repl.console.welcome.shortcutMissing")
    }
  }

  def getModuleRoot(@NotNull moduleFilePath: String): String = {
    val lastIndexOf = moduleFilePath.lastIndexOf("/")
    moduleFilePath.substring(0, lastIndexOf + 1) // scalastyle:ignore
  }

  /**
   * Creates the initial REPL commands file if it does not exist yet, otherwise does nothing.
   */
  def createInitialReplCommandsFile(@NotNull module: Module): Unit = {
    val commands = getInitialReplCommands(module)
    val file = Paths
      .get(getModuleDirectory(module), PluginSettings.MODULE_REPL_INITIAL_COMMANDS_FILE_NAME)
      .toFile
    if (commands.nonEmpty && !file.exists) {
      try FileUtils.writeLines(file, StandardCharsets.UTF_8.name, asJavaCollection(commands))
      catch {
        case ex: IOException => Logger.error("Could not write REPL initial commands file", ex)
      }
    }
  }

  def initialReplCommandsFileExists(@NotNull module: Module): Boolean =
    Paths
      .get(getModuleDirectory(module), PluginSettings.MODULE_REPL_INITIAL_COMMANDS_FILE_NAME)
      .toFile
      .exists

  @NotNull
  def getInitialReplCommands(module: Module): Array[String] = {
    Option(
      PluginSettings
        .getInstance()
        .getMainViewModel(module.getProject)
        .courseViewModel
        .get())
      .map(_.getModel.getReplInitialCommands.getOrDefault(module.getName, Array.empty))
      .getOrElse(Array.empty)

  }

  def isTopLevelModule(module: Module): Boolean = module.getName.equals(module.getProject.getName)
}
