// The reason for this class being in a separate package is that the runLine method
// uses the ScalaLanguageConsole.textSent() method, which is package private. Therefore, in order to
// call it, we must be in the same package as the console: org.jetbrains.plugins.scala.console.

package org.jetbrains.plugins.scala.console.apluscourses

import fi.aalto.cs.apluscourses.intellij.Repl
import javax.swing.SwingUtilities
import org.jetbrains.plugins.scala.console.ScalaConsoleInfo

object ScalaExecutor {
  /**
   * Runs a single line of Scala code in the context of the provided REPL console.
   * @param console An instance of our A+ enhanced REPL.
   * @param command A single line (no newlines) of Scala code to execute.
   */
  def runLine(console: Repl, command: String): Unit = {
    val processHandler = ScalaConsoleInfo.getProcessHandler(console.getConsoleEditor)
    if (processHandler == null) {
      return // scalastyle:ignore
    }

    val outputStream = processHandler.getProcessInput
    outputStream.write((command + "\n").getBytes)
    outputStream.flush()

    // this must be invoked from EDT because it accesses the IntelliJ PSI
    SwingUtilities.invokeLater(() => console.textSent(command))
  }
}
