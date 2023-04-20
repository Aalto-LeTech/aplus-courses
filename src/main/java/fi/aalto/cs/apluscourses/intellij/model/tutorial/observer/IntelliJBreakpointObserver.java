package fi.aalto.cs.apluscourses.intellij.model.tutorial.observer;

import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointListener;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.component.IntelliJTutorialComponent;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.util.XDebuggerUtil;
import fi.aalto.cs.apluscourses.model.tutorial.CodeContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("rawtypes")
public class IntelliJBreakpointObserver extends IntelliJMessageBusObserverBase<XBreakpointListener> {
  private final @NotNull CodeContext codeContext;
  private final @NotNull XBreakpointListener listener;


  public IntelliJBreakpointObserver(@NotNull IntelliJTutorialComponent<?> component) {
    super(XBreakpointListener.TOPIC, component);
    this.codeContext = component.getCodeContext();
    this.listener = new MyListener();
  }

  @Override
  protected @NotNull XBreakpointListener getMessageListener() {
    return listener;
  }

  private class MyListener implements XBreakpointListener {
    @Override
    public void breakpointAdded(@NotNull XBreakpoint breakpoint) {
      if (XDebuggerUtil.containsSourcePosition(codeContext, breakpoint.getSourcePosition())) {
        fire();
      }
    }
  }
}
