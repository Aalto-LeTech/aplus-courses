package org.jetbrains.plugins.scala.console.apluscourses

import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys}
import com.intellij.openapi.util.TextRange
import org.jetbrains.plugins.scala.console.ScalaConsoleInfo
import org.jetbrains.plugins.scala.console.actions.ScalaConsoleExecuteAction
import org.jetbrains.plugins.scala.inWriteAction
import java.io.OutputStream

class ConsoleExecuteAction extends ScalaConsoleExecuteAction {
  override def actionPerformed(e: AnActionEvent): Unit = {
    val editor = e.getData(CommonDataKeys.EDITOR)
    if (editor == null) return

    val console           = ScalaConsoleInfo.getConsole(editor)
    val processHandler    = ScalaConsoleInfo.getProcessHandler(editor)
    val historyController = ScalaConsoleInfo.getController(editor)

    val document = console.getEditorDocument
    val text = document.getText

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

    val outputStream: OutputStream = processHandler.getProcessInput
    val finalText = text.split('\n').mkString("\n")

    if (finalText == "\n") {
      outputStream.write(finalText.getBytes)
    } else {
      val textEndSequence = "\u001b[201~".getBytes

      var bytes: Array[Byte] = finalText.getBytes
      val contentLength = bytes.length + textEndSequence.length

      if (contentLength % 64 != 0) {
        bytes = bytes ++ Array.fill[Byte](64 - (contentLength % 64))(0x20)
      }

      outputStream.write("\u001b[200~".getBytes)
      outputStream.flush()

      outputStream.write(bytes ++ textEndSequence ++ "\n".getBytes)
    }

    outputStream.flush()
    console.textSent(finalText)
  }
}
