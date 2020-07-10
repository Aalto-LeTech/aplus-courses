package fi.aalto.cs.apluscourses.intellij

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.module.Module
import org.jetbrains.plugins.scala.console.ScalaLanguageConsole

class MyScalaLanguageConsole(module: Module)
  extends ScalaLanguageConsole(module: Module) {
  override def print(text: String, contentType: ConsoleViewContentType): Unit = {
    var updatedText = text
    if (text.contains("Welcome")){
      updatedText = "blaaaaah! "
    }

    super.print(updatedText, contentType)
  }
}
