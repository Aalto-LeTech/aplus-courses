package fi.aalto.cs.apluscourses.intellij.actions

import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys}
import com.intellij.openapi.module.{ModuleManager, ModuleUtilCore}
import org.jetbrains.plugins.scala.console.actions.RunConsoleAction

class REPLAction extends RunConsoleAction {

  override def actionPerformed(e: AnActionEvent): Unit = {
    println("inside REPLAction: yeah, baby, yeah!")

    val dataContext = e.getDataContext
    val file = CommonDataKeys.PSI_FILE.getData(dataContext)
    val module = ModuleUtilCore.findModuleForFile(file)

    println("current module: " + module.getName + " for file: " + file)

    val allModules = ModuleManager.getInstance(e.getProject).getModules.filter(_.getModuleTypeName == "JAVA_MODULE")

    println("available modules:")
    allModules.foreach(ev => println("  - " + ev.getName))

    super.actionPerformed(e)
  }
}
