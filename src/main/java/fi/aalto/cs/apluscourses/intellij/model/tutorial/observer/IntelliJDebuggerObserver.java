package fi.aalto.cs.apluscourses.intellij.model.tutorial.observer;

import static java.util.Objects.requireNonNull;

import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebugSessionListener;
import com.intellij.xdebugger.XDebuggerManager;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.IntelliJTutorialClientObject;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.util.XDebuggerUtil;
import fi.aalto.cs.apluscourses.model.tutorial.CodeContext;
import fi.aalto.cs.apluscourses.model.tutorial.Observer;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJDebuggerObserver extends Observer implements IntelliJTutorialClientObject {
  private final @NotNull XDebugSessionListener listener;
  private final @NotNull CodeContext codeContext;
  private @Nullable XDebugSession activeSession;

  public IntelliJDebuggerObserver(@NotNull String action, @NotNull TutorialComponent component) {
    super(component);
    codeContext = component.getCodeContext();
    listener = chooseListener(action);
  }

  private @NotNull XDebugSessionListener chooseListener(@NotNull String action) {
    switch (action) {
      case DEBUGGER_PAUSE:
        return new PauseListener();
      case DEBUGGER_RESUME:
        return new ResumeListener();
      default:
        throw new IllegalArgumentException("Unknown debugger action: " + action);
    }
  }

  @Override
  public void activate() {
    var project = requireNonNull(getProject(), "Project must not be null");
    var session = requireNonNull(XDebuggerManager.getInstance(project).getCurrentSession(),
        "No debug session ongoing.");
    activeSession = session;
    session.addSessionListener(listener);
  }

  @Override
  public void deactivate() {
    var session = Objects.requireNonNull(activeSession, "No active session");
    session.removeSessionListener(listener);
    activeSession = null;
  }

  private void onAction() {
    if (Optional.ofNullable(activeSession).filter(this::isInRelevantPosition).isPresent()) {
      fire();
    }
  }

  private boolean isInRelevantPosition(@NotNull XDebugSession session) {
    return XDebuggerUtil.containsSourcePosition(codeContext, session.getCurrentPosition());
  }

  private class PauseListener implements XDebugSessionListener {
    @Override
    public void sessionPaused() {
      onAction();
    }
  }

  private class ResumeListener implements XDebugSessionListener {
    public void sessionResumed() {
      onAction();
    }
  }
}
