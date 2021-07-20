package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.console.ScalaConsoleInfo;
import org.jetbrains.plugins.scala.console.ScalaLanguageConsole;

public abstract class ReplListener implements ActivitiesListener, ProcessListener {
  @NotNull
  private final ListenerCallback callback;

  @NotNull
  private final Project project;

  private ProcessHandler processHandler;

  private ScalaLanguageConsole console;

  /**
   * A constructor.
   */
  protected ReplListener(@NotNull ListenerCallback callback,
                         @NotNull Project project) {
    this.callback = callback;
    this.project = project;
  }

  @Override
  public boolean registerListener() {
    console = ScalaConsoleInfo.getConsole(project);
    processHandler = ScalaConsoleInfo.getProcessHandler(project);

    processHandler.addProcessListener(this);

    return false;
  }

  @Override
  public void unregisterListener() {
    if (processHandler != null) {
      processHandler.removeProcessListener(this);
      processHandler = null;
    }
  }

  @Override
  public void startNotified(@NotNull ProcessEvent event) {
    // Do nothing
  }

  @Override
  public void processTerminated(@NotNull ProcessEvent event) {
    // Do nothing
  }

  @Override
  public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
    if (isCorrect(event)) {
      callback.callback();
    }
  }

  protected abstract boolean isCorrect(@NotNull ProcessEvent event);

  protected String getLastLine() {
    var lines = console.getHistory().split("\n");
    var lastLine = lines[lines.length - 1];
    var index = lastLine.indexOf("null");
    if (index != -1) {
      lastLine = lastLine.substring(index + 4);
    }
    return lastLine.replaceAll("\\s", "");
  }

  protected String getEventText(@NotNull ProcessEvent event) {
    var text = event.getText();
    return text.substring(text.indexOf(" ") + 1);
  }

}
