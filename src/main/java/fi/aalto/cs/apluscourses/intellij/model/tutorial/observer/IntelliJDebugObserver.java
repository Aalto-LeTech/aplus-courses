package fi.aalto.cs.apluscourses.intellij.model.tutorial.observer;

import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerManagerListener;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import org.jetbrains.annotations.NotNull;

public class IntelliJDebugObserver extends IntelliJMessageBusObserverBase<XDebuggerManagerListener> {

  private final @NotNull XDebuggerManagerListener listener;

  public IntelliJDebugObserver(@NotNull String action, @NotNull TutorialComponent component) {
    super(XDebuggerManager.TOPIC, component);
    listener = chooseListener(action);
  }

  protected @NotNull XDebuggerManagerListener chooseListener(@NotNull String action) {
    switch (action) {
      case DEBUG_START:
        return new StartListener();
      case DEBUG_STOP:
        return new StopListener();
      default:
        throw new IllegalArgumentException("Unknown debug action: " + action);
    }
  }

  @Override
  protected @NotNull XDebuggerManagerListener getMessageListener() {
    return listener;
  }

  private class StartListener implements XDebuggerManagerListener {
    @Override
    public void processStarted(@NotNull XDebugProcess debugProcess) {
      fire();
    }
  }

  private class StopListener implements XDebuggerManagerListener {
    @Override
    public void processStopped(@NotNull XDebugProcess debugProcess) {
      fire();
    }
  }
}
