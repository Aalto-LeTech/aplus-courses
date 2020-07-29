package fi.aalto.cs.apluscourses.intellij.utils

import java.nio.file.{Files, Path}

import com.intellij.openapi.actionSystem.{CommonDataKeys, DataContext}
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.module.{Module, ModuleUtilCore}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.OrderEnumerator
import com.intellij.openapi.util.io.FileUtilRt
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings
import org.jetbrains.annotations.NotNull

object ModuleUtils {

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
      case 1 => s"Auto-imported package [${imports.head}] for your convenience."
      case _ => s"Auto-imported packages [${imports.mkString(", ")}] for your convenience."
    }

  def getUpdatedText(@NotNull module: Module,
                     @NotNull commands: Array[String],
                     @NotNull originalText: String): String = {
    val runConsoleShortCut = getPrettyKeyMapString("Scala.RunConsole")
    val executeConsoleShortCut = getPrettyKeyMapString("ScalaConsole.Execute")
    val reRunShortCut = getPrettyKeyMapString("Rerun")
    val editorUpShortCut = getPrettyKeyMapString("EditorUp")
    val editorDownShortCut = getPrettyKeyMapString("EditorDown")

    val commonText = s"Write a line (or more) of Scala and press [${executeConsoleShortCut}] to run " +
      s"it. Use [${editorUpShortCut}] and [${editorDownShortCut}] to scroll through your earlier " +
      s"inputs. \nChanges to the module are not loaded automatically. If you edit the files, restart" +
      s" the REPL with [${reRunShortCut}] or the icon on the left. \n"

    if (isTopLevelModule(module)) {
      s"${commonText}${originalText}Note: This REPL session is not linked to any course module. To " +
        s"use a module from the REPL, select the module and press [${runConsoleShortCut}] to launch" +
        s" a new session."
    } else {
      val validCommands = commands.filter(command => naiveValidate(command))
      val clearedCommands = clearCommands(validCommands)
      val commandsText = getCommandsText(clearedCommands)

      s"Loaded A+ Courses module [${module.getName}]. ${commandsText}\n${commonText}${originalText}"
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
        .filter(part => !part.equals("pressed"))
        .map(_.toLowerCase)
        .map(_.capitalize)
        .mkString("+")
    } else {
      "SHORTCUT MISSING"
    }
  }

  def getModuleRoot(@NotNull moduleFilePath: String): String = {
    val lastIndexOf = moduleFilePath.lastIndexOf("/")
    moduleFilePath.substring(0, lastIndexOf + 1) // scalastyle:ignore
  }

  def initialReplCommandsFileExist(@NotNull replCommandsFileName: String,
                                   @NotNull moduleFilePath: String): Boolean = {
    val moduleDir = getModuleRoot(moduleFilePath)
    Files.exists(Path.of(moduleDir + replCommandsFileName))
  }

  @NotNull
  def getReplInitialCommandsForModule(module: Module): Array[String] = {
    PluginSettings
      .getInstance()
      .getMainViewModel(module.getProject)
      .courseViewModel
      .get()
      .getModel
      .getReplInitialCommands
      .getOrDefault(module.getName, Array.empty)
  }

  def isTopLevelModule(module: Module): Boolean = module.getName.equals(module.getProject.getName)
}
