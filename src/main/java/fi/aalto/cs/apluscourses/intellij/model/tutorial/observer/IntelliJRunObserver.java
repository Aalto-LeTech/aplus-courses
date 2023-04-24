package fi.aalto.cs.apluscourses.intellij.model.tutorial.observer;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import org.jetbrains.annotations.NotNull;

public class IntelliJRunObserver extends IntelliJMessageBusObserverBase<ExecutionListener> {

  private final @NotNull ExecutionListener listener;

  public IntelliJRunObserver(@NotNull TutorialComponent component) {
    super(ExecutionManager.EXECUTION_TOPIC, component);
    listener = new MyListener();
  }

  @Override
  protected @NotNull ExecutionListener getMessageListener() {
    return listener;
  }

  private class MyListener implements ExecutionListener {
    @Override
    public void processStarted(@NotNull String executorId,
                               @NotNull ExecutionEnvironment env,
                               @NotNull ProcessHandler handler) {
      fire();
    }
  }
}
