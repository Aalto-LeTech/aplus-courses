package fi.aalto.cs.apluscourses.intellij.utils

import com.intellij.openapi.actionSystem.{CommonDataKeys, DataContext}
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.module.{Module, ModuleUtilCore}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.OrderEnumerator
import com.intellij.openapi.util.io.FileUtilRt
import org.jetbrains.annotations.NotNull

object ReplUtils {

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

  def ignoreFileInProjectView(@NotNull fileName: String, @NotNull project: Project) = {
    // this stuff here hides any desired file from project view (be it here for now)
    val manager = FileTypeManager.getInstance()
    val existing = manager.getIgnoredFilesList
    val r: Runnable = () => manager.setIgnoredFilesList(existing + fileName + ";")
    WriteCommandAction.runWriteCommandAction(project, r)
  }

  def naiveValidate(@NotNull command: String): Boolean =
    command.matches("import\\so1\\.[a-z]*(\\_|\\._)$")

  def clearCommands(@NotNull imports: Array[String]): Array[String] =
    imports
      .clone
      .map(_.replace("import ", ""))
      .map(_.replace("._", ""))

  def getCommandsText(@NotNull imports: Array[String]) =
    imports.length match {
      case 0 => ""
      case 1 => "Auto-imported package [" + imports(0) + "] for your convenience."
      case _ => "Auto-imported packages [" + imports.mkString(", ") + "] for your convenience."
    }

  def getUpdatedText(@NotNull moduleName: String,
                     @NotNull commands: Array[String],
                     @NotNull originalText: String) = {
    val validCommands = commands.filter(command => naiveValidate(command))
    val clearedCommands = clearCommands(validCommands)
    val commandsText = getCommandsText(clearedCommands)

    "Loaded A+ Courses module [" + moduleName + "]. " + commandsText + "\nWrite a line (or more) of " +
      "Scala and press [Ctrl+Enter] to run it. Use [Up] and [Down] to scroll through your earlier " +
      "inputs. \nChanges to the module are not loaded automatically. If you edit the files, restart" +
      " the REPL with [Ctrl+F5] or the icon on the left. \n" + originalText
  }

  def getTheModuleRoot(@NotNull moduleFilePath: String) = {
    val lastIndexOf = moduleFilePath.lastIndexOf("/")
    moduleFilePath.substring(0, lastIndexOf + 1)
  }

  def initialReplCommandsFileExist(@NotNull replCommandsFileName: String,
                                   @NotNull moduleFilePath: String): Boolean = {
    val moduleDir = getTheModuleRoot(moduleFilePath)
    false
  }
}
