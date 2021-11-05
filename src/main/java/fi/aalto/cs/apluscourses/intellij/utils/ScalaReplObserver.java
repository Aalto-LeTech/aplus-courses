package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.execution.impl.ExecutionManagerImpl;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.utils.DelegateTimerTask;
import java.util.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.console.ScalaConsoleInfo;

public class ScalaReplObserver {

  private final @NotNull Project project;
  private final @NotNull String module;
  private final @NotNull Callback callback;
  private final @NotNull Timer timer = new Timer();

  /**
   * Instantiates a new ScalaReplObserver.
   *
   * @param project  Project.
   * @param module   Name of the module.
   * @param callback Who to call, when the REPL is open.
   */
  public ScalaReplObserver(@NotNull Project project, @NotNull String module, @NotNull Callback callback) {
    this.project = project;
    this.module = module;
    this.callback = callback;
  }

  public void start() {
    timer.schedule(new DelegateTimerTask(this::checkIsReplOpen), 500, 500);
  }

  public void stop() {
    timer.cancel();
  }

  private void checkIsReplOpen() {
    if (isReplOpen()) {
      stop();
      callback.onReplOpen();
    }
  }

  public boolean isReplOpen() {
    return isReplOpen(project, module);
  }

  /**
   * Returns true if the REPL for a given module is open.
   */
  public static boolean isReplOpen(@NotNull Project project, @NotNull String module) {
    return ScalaConsoleInfo.getConsole(project) != null
        && ExecutionManagerImpl.getAllDescriptors(project).stream().anyMatch(d -> d.getDisplayName().contains("REPL")
        && d.getDisplayName().contains(module));
  }

  public interface Callback {
    void onReplOpen();
  }
}
