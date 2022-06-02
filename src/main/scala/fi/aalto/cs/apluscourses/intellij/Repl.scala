package fi.aalto.cs.apluscourses.intellij

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.module.Module
import fi.aalto.cs.apluscourses.intellij.utils.ModuleUtils.{getInitialReplCommands, getUpdatedText}
import fi.aalto.cs.apluscourses.intellij.utils.ReplChangesObserver
import fi.aalto.cs.apluscourses.ui.ReplBannerPanel
import org.jetbrains.plugins.scala.console.ScalaLanguageConsole

import java.awt.AWTEvent
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Toolkit
import javax.swing.SwingUtilities

class Repl(module: Module) extends ScalaLanguageConsole(module: Module) {
  private var initialReplWelcomeMessageHasBeenReplaced: Boolean = false
  private val initialReplWelcomeMessageToBeReplaced: String =
    "Type in expressions for evaluation. Or try :help.\n"

  private val banner = new ReplBannerPanel()
  banner.setVisible(false)
  add(banner, BorderLayout.NORTH)

  // creating a new REPL resets the "module changed" state
  ReplChangesObserver.onStartedRepl(module)

  Toolkit.getDefaultToolkit.addAWTEventListener((event: AWTEvent) => {
    if (SwingUtilities.isDescendingFrom(event.getSource.asInstanceOf[Component], this)) {
      banner.setVisible(ReplChangesObserver.hasModuleChanged(module))
    }
  }, AWTEvent.FOCUS_EVENT_MASK)

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
