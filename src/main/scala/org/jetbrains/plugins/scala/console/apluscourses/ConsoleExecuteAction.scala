// scalastyle:off
// This file uses modified code based on the IntelliJ Scala plugin. Original code can be found here:
// https://github.com/JetBrains/intellij-scala/blob/bd2ec19ced511fd2f27459ca733dde5cb432aba6/scala/scala-impl/src/org/jetbrains/plugins/scala/console/actions/ScalaConsoleExecuteAction.scala
// scalastyle:on

// The reason for this class being in a separate package is that the ConsoleExecuteAction class
// uses the ScalaLanguageConsole.textSent() method, which is package private. Therefore, in order to
// call it, we must be in the same package as the console: org.jetbrains.plugins.scala.console.

package org.jetbrains.plugins.scala.console.apluscourses

import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys}
import com.intellij.openapi.util.TextRange
import fi.aalto.cs.apluscourses.intellij.Repl
import org.jetbrains.plugins.scala.console.ScalaConsoleInfo
import org.jetbrains.plugins.scala.console.actions.ScalaConsoleExecuteAction
import org.jetbrains.plugins.scala.inWriteAction

import java.io.OutputStream
import javax.swing.SwingUtilities

class ConsoleExecuteAction extends ScalaConsoleExecuteAction {
  // We achieve proper multiline support by surrounding the REPL commands by special
  // ANSI sequences indicating "bracketed paste". We exploit the fact that Scala 3 REPL
  // uses the JLine 3 library, which explicitly supports the bracketed paste sequences.

  // "Bracketed paste" means that newlines encountered in the string surrounded by paste markers
  // should not be treated as end-of-statement markers, but rather mere parts of the statement.
  // See https://cirw.in/blog/bracketed-paste for details.
  private val BeginPaste = "\u001b[200~".getBytes
  private val EndPaste = "\u001b[201~".getBytes

  // This is a "feature" of JLine - it has support for various terminals, but for IntelliJ console
  // there isn't one, and therefore JLine defaults to "DumbTerminal", which is a terminal with no
  // special features.
  // When inserting bracketed paste sequences, the JLine library scans the input for the
  // "bracketed paste end" sequence by reading from the terminal in 64-byte chunks.
  // JLine erroneously blocks until the whole chunk is read, therefore we need to pad the data
  // to 64 characters in order to appease JLine. (This only occurs for DumbTerminals)
  private val JLineBufferLength = 64

  private def padTextToBlockLength(text: String): Array[Byte] = {
    var userInputBytes: Array[Byte] = text.getBytes
    val contentLength = userInputBytes.length + EndPaste.length

    // We pad the input with some "neutral" character - a one that will have no side effects
    // even if we add fifty of these. Space seems to be a good candidate.
    if (contentLength % JLineBufferLength != 0) {
      userInputBytes = userInputBytes ++
        Array.fill[Byte](JLineBufferLength - (contentLength % JLineBufferLength))(' ')
    }

    userInputBytes
  }

  override def actionPerformed(e: AnActionEvent): Unit = {
    val editor = e.getData(CommonDataKeys.EDITOR)
    if (editor == null) {
      return // scalastyle:ignore
    }

    val console = ScalaConsoleInfo.getConsole(editor)
    val processHandler = ScalaConsoleInfo.getProcessHandler(editor)
    val historyController = ScalaConsoleInfo.getController(editor)

    val document = console.getEditorDocument
    val text = document.getText

    // We should perform our multiline fixing only for our custom REPL that is running Scala 3.
    // Non-A+ REPLs or those that host Scala 2 should not be modified.
    // Additionally, if the text has no newlines, we don't need to do the bracketed paste.
    if (!console.isInstanceOf[Repl] || !console.asInstanceOf[Repl].isScala3REPL ||
        !text.exists(c => c == '\n' || c == '\r')) {
      super.actionPerformed(e)
      return // scalastyle:ignore
    }

    // Process input and add to history
    inWriteAction {
      val range: TextRange = new TextRange(0, document.getTextLength)
      editor.getSelectionModel.setSelection(range.getStartOffset, range.getEndOffset)
      // note: it uses `range` instead ot just editor `text` because under the hood it splits actual editor content
      // according to the highlighter attributes and passes correct ContentType to the history console
      console.addToHistory(range, console.getConsoleEditor, true)
      // without this line there will be a slight blinking of user input code SCL-16655
      // see com.intellij.execution.impl.ConsoleViewImpl.print
      console.flushDeferredText()
      historyController.addToHistory(text)

      editor.getCaretModel.moveToOffset(0)
      editor.getDocument.setText("")
    }

    // the "start paste" sequence has to be in a separate write call, otherwise JLine won't
    // pick it up properly; it seems to be yet another quirk of JLine and DumbTerminal
    val outputStream = processHandler.getProcessInput
    outputStream.write(BeginPaste)
    outputStream.flush()

    outputStream.write(padTextToBlockLength(text) ++ EndPaste ++ "\n".getBytes)
    outputStream.flush()

    console.textSent(text)
  }
}

object ConsoleExecuteAction {
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
