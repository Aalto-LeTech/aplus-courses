package fi.aalto.cs.apluscourses.intellij

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.module.Module
import fi.aalto.cs.apluscourses.intellij.utils.ReplUtils.{getReplInitialCommandsForModule, getUpdatedText}
import org.jetbrains.plugins.scala.console.ScalaLanguageConsole

class Repl(module: Module) extends ScalaLanguageConsole(module: Module) {

  override def print(text: String, contentType: ConsoleViewContentType) = {
    var updatedText = text

    if (text.contains("Type in expressions")) {
      val commands = getReplInitialCommandsForModule(module)
      updatedText = getUpdatedText(module, commands, text)
    }

    super.print(updatedText, contentType)
  }
}
