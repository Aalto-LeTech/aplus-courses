package fi.aalto.cs.apluscourses.intellij

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.module.Module
import fi.aalto.cs.apluscourses.intellij.utils.ReplUtils.getUpdatedText
import org.jetbrains.plugins.scala.console.ScalaLanguageConsole

class Repl(module: Module) extends ScalaLanguageConsole(module: Module) {

  override def print(text: String, contentType: ConsoleViewContentType): Unit = {
    var updatedText = text

    val moduleName = module.getName
    //  read from the course config file or smth
    val commands = Array("import o1._", "import o1.train._")

    if (text.contains("Type in expressions")) {
      updatedText = getUpdatedText(moduleName, commands, text)
    }

    super.print(updatedText, contentType)
  }
}
