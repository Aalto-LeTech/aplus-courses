package fi.aalto.cs.apluscourses.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.plugins.scala.console.actions.RunConsoleAction

class REPLAction extends RunConsoleAction{

  override def actionPerformed(e: AnActionEvent): Unit = {
    println("inside REPLAction: yeah, baby, yeah!")
    super.actionPerformed(e)
  }
}
