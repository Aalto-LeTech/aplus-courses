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
import fi.aalto.cs.apluscourses.intellij.utils.ScalaReplObserver;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.ScalaLanguage;
import org.jetbrains.plugins.scala.console.ScalaConsoleInfo;
import org.jetbrains.plugins.scala.console.ScalaLanguageConsole;

public abstract class ScalaReplListener extends ActivitiesListenerBase<Boolean>
    implements ProcessListener, ScalaReplObserver.Callback {

  @NotNull
  private final Project project;


  private ProcessHandler processHandler;

  private ScalaLanguageConsole console;

  private int historyLength = 0;

  protected String lastInput;

  protected PsiFile file;

  private ScalaReplObserver scalaReplObserver;

  /**
   * A constructor.
   */
  protected ScalaReplListener(@NotNull ListenerCallback callback,
                              @NotNull Project project,
                              @NotNull String module) {
    super(callback);
    scalaReplObserver = new ScalaReplObserver(project, module, this);
    this.project = project;
  }

  @Override
  protected void registerListenerOverride() {
    scalaReplObserver.start();
  }

  @Override
  public void onReplOpen() {
    console = ScalaConsoleInfo.getConsole(project);
    processHandler = ScalaConsoleInfo.getProcessHandler(project);
    processHandler.addProcessListener(this);
  }

  @Override
  protected void unregisterListenerOverride() {
    scalaReplObserver.stop();
    if (processHandler != null) {
      processHandler.removeProcessListener(this);
      processHandler = null;
    }
  }

  @Override
  protected boolean checkOverride(Boolean param) {
    return param;
  }

  @Override
  protected Boolean getDefaultParameter() {
    return false;
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
      check(true);
    }
  }

  protected abstract boolean isCorrect(@NotNull ProcessEvent event);

  protected String getEventText(@NotNull ProcessEvent event) {
    var text = event.getText();
    return text.substring(text.indexOf(" ") + 1);
  }
}
