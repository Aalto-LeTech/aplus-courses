package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.ScalaLanguage;
import org.jetbrains.plugins.scala.console.ScalaConsoleInfo;
import org.jetbrains.plugins.scala.console.ScalaLanguageConsole;

public abstract class ScalaReplListener implements ActivitiesListener, ProcessListener {
  @NotNull
  private final ListenerCallback callback;

  @NotNull
  private final Project project;

  @NotNull
  private final String module;

  private ProcessHandler processHandler;

  private ScalaLanguageConsole console;

  private int historyLength = 0;

  protected String lastInput;

  protected PsiFile file;

  private RunReplListener runReplListener;

  /**
   * A constructor.
   */
  protected ScalaReplListener(@NotNull ListenerCallback callback,
                              @NotNull Project project,
                              @NotNull String module) {
    this.callback = callback;
    this.project = project;
    this.module = module;
  }

  @Override
  public boolean registerListener() {
    if (!RunReplListener.isReplOpen(project, module)) {
      runReplListener = new RunReplListener(this::registerListener, project, module);
      if (!runReplListener.registerListener()) {
        return false;
      }
    }

    if (runReplListener != null) {
      runReplListener.unregisterListener();
    }

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
    var history = console.getHistory();
    if (history.length() != historyLength) {
      var input = history.substring(historyLength).trim();
      if (input.length() < 4 || !"null".equals(input.substring(input.length() - 4))) {
        var index = input.indexOf("null");
        if (index != -1) {
          input = input.substring(index + 4);
        }
        lastInput = input;
        ApplicationManager.getApplication().runReadAction((Computable<PsiFile>) () -> file = PsiFileFactory
            .getInstance(project)
            .createFileFromText(ScalaLanguage.INSTANCE, lastInput)
        );
      }
      historyLength = history.length();
    }
    if (isCorrect(event)) {
      callback.callback();
    }
  }

  protected abstract boolean isCorrect(@NotNull ProcessEvent event);

  protected String getEventText(@NotNull ProcessEvent event) {
    var text = event.getText();
    return text.substring(text.indexOf(" ") + 1);
  }

}
