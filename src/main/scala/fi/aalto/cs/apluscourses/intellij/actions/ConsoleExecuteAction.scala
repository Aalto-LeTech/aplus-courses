package fi.aalto.cs.apluscourses.intellij.actions

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

    val finalText = text.split('\n').mkString("\n") + "\n"
    val outputStream: OutputStream = processHandler.getProcessInput
    val bytes: Array[Byte] = finalText.getBytes
    outputStream.write(bytes)
    outputStream.flush()

    // cannot do that directly because console is package-private?
    // console.textSent(finalText)
    val method = console.getClass.getSuperclass.getDeclaredMethod("textSent", classOf[String])
    method.invoke(console, finalText)
  }
}
