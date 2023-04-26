package fi.aalto.cs.apluscourses.intellij.model.tutorial.observer;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import org.jetbrains.annotations.NotNull;

public class IntelliJRunObserver extends IntelliJMessageBusObserverBase<ExecutionListener> {

  private final @NotNull ExecutionListener listener;

  public IntelliJRunObserver(@NotNull String action, @NotNull TutorialComponent component) {
    super(ExecutionManager.EXECUTION_TOPIC, component);
    listener = chooseListener(action);
  }

  private @NotNull ExecutionListener chooseListener(@NotNull String action) {
    switch (action) {
      case "launch":
        return new StartScheduledListener();
      case "start":
        return new StartedListener();
      case "finish":
        return new FinishedListener();
      default:
        throw new IllegalArgumentException("Unknown execution action: " + action);
    }
  }

  private void onAction() {
    fire();
  }

  @Override
  protected @NotNull ExecutionListener getMessageListener() {
    return listener;
  }

  private class StartScheduledListener implements ExecutionListener {
    @Override
    public void processStartScheduled(@NotNull String executorId, @NotNull ExecutionEnvironment env) {
      onAction();
    }
  }

  private class StartedListener implements ExecutionListener {
    @Override
    public void processStarted(@NotNull String executorId,
                               @NotNull ExecutionEnvironment env,
                               @NotNull ProcessHandler handler) {
      onAction();
    }
  }

  private class FinishedListener implements ExecutionListener {
    @Override
    public void processTerminated(@NotNull String executorId,
                                  @NotNull ExecutionEnvironment env,
                                  @NotNull ProcessHandler handler,
                                  int exitCode) {
      onAction();
    }
  }
}
