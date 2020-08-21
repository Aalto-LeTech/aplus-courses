package fi.aalto.cs.apluscourses.intellij

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.module.Module
import fi.aalto.cs.apluscourses.intellij.utils.ModuleUtils.{getInitialReplCommands, getUpdatedText}
import org.jetbrains.plugins.scala.console.ScalaLanguageConsole

class Repl(module: Module) extends ScalaLanguageConsole(module: Module) {
  private var initialReplWelcomeMessageHasBeenReplaced: Boolean = false
  private val initialReplWelcomeMessageToBeReplaced: String =
    "Type in expressions for evaluation. Or try :help.\n"

  override def print(text: String, contentType: ConsoleViewContentType): Unit = {
    var updatedText = text

    if (text.equals(initialReplWelcomeMessageToBeReplaced)
      && !initialReplWelcomeMessageHasBeenReplaced) {
      val commands = getInitialReplCommands(module)
      updatedText = getUpdatedText(module, commands, text)
      initialReplWelcomeMessageHasBeenReplaced = true
    }

    super.print(updatedText, contentType)
  }
}
