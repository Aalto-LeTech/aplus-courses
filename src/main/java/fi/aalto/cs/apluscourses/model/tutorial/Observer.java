package fi.aalto.cs.apluscourses.model.tutorial;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Observer extends TutorialClientObjectBase {
  public static final String FILE_OPEN = "open";
  public static final String FILE_CLOSE = "close";
  public static final String BUILD_START = "start";
  public static final String BUILD_FINISH = "finish";
  public static final String DEBUG_START = "start";
  public static final String DEBUG_STOP = "stop";
  public static final String DEBUGGER_PAUSE = "pause";
  public static final String DEBUGGER_RESUME = "resume";
  public static final String RUN_LAUNCH = "launch";
  public static final String RUN_START = "start";
  public static final String RUN_FINISH = "finish";

  private @Nullable Runnable handler;

  protected Observer(@NotNull TutorialComponent component) {
    super(component);
  }

  protected void fire() {
    Optional.ofNullable(handler).ifPresent(Runnable::run);
  }

  public void setHandler(@NotNull Runnable handler) {
    if (this.handler != null) {
      throw new IllegalStateException("Handler is already set.");
    }
    this.handler = handler;
  }
}
