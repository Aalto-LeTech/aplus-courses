package fi.aalto.cs.apluscourses.intellij

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModuleRootManager
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings
import fi.aalto.cs.apluscourses.intellij.utils.ModuleUtils.{getInitialReplCommands, getUpdatedText}
import fi.aalto.cs.apluscourses.intellij.utils.{ModuleUtils, ReplChangesObserver}
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

  // Do not show the warning banner for non-A+ courses
  if (PluginSettings.getInstance.getCourseProject(module.getProject) != null) {
    // creating a new REPL resets the "module changed" state
    ReplChangesObserver.onStartedRepl(module)

    Toolkit.getDefaultToolkit.addAWTEventListener((event: AWTEvent) => {
      if (SwingUtilities.isDescendingFrom(event.getSource.asInstanceOf[Component], this)) {
        banner.setVisible(ReplChangesObserver.hasModuleChanged(module))
      }
    }, AWTEvent.FOCUS_EVENT_MASK)
  }

  // We need this here because the overridden ConsoleExecuteAction needs to determine whether
  // the console is hosting a Scala 3 REPL or something else
  val isScala3REPL: Boolean = ModuleUtils.nonEmpty(
    ModuleRootManager.getInstance(module)
      .orderEntries()
      .librariesOnly()
      .satisfying(x => x.getPresentableName.contains("scala3-") || x.getPresentableName.contains("scala-sdk-3."))
  )

  override def print(text: String, contentType: ConsoleViewContentType): Unit = {
    var updatedText = text

    if (text.equals(initialReplWelcomeMessageToBeReplaced)
      && !initialReplWelcomeMessageHasBeenReplaced) {
      val commands = getInitialReplCommands(module)
      updatedText = getUpdatedText(module, commands, text)
      initialReplWelcomeMessageHasBeenReplaced = true
    }

    // In Scala 3 REPL, the "scala>" prompt is colored blue by sending appropriate ANSI sequences
    // This is not handled correctly by Scala plugin which expects the prompt to be sent with
    // the NORMAL_OUTPUT attributes; this breaks the REPL state machine
    // Therefore we override the text attributes for the prompt. The only unintended consequence
    // of this fix is that output lines equal to the prompt will also lose their text attributes.
    super.print(updatedText, if (text.trim == "scala>")
      ConsoleViewContentType.NORMAL_OUTPUT else contentType)
  }
}
