package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.execution.impl.ExecutionManagerImpl;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import java.util.Timer;
import java.util.TimerTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.console.ScalaConsoleInfo;

public class RunReplListener implements ActivitiesListener {
  private final ListenerCallback callback;
  private final Project project;
  private final String module;
  private Timer timer;

  /**
   * A constructor for a listener that listens for a REPL of a given module opening.
   */
  public RunReplListener(@NotNull ListenerCallback callback,
                         @NotNull Project project,
                         @NotNull String module) {
    this.callback = callback;
    this.project = project;
    this.module = module;
  }

  /**
   * Creates an instance of RunReplListener based on the provided arguments.
   */
  public static RunReplListener create(ListenerCallback callback,
                                       Project project, Arguments arguments) {
    return new RunReplListener(callback, project,
        arguments.getString("module"));
  }

  @Override
  public boolean registerListener() {
    if (isReplOpen(project, module)) {
      return true;
    }
    timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        if (isReplOpen(project, module)) {
          timer.cancel();
          callback.callback();
        }
      }
    }, 500, 500);
    return false;
  }

  @Override
  public void unregisterListener() {
    if (timer != null) {
      timer.cancel();
      timer = null;
    }
  }

  public static boolean isReplOpen(@NotNull Project project, @NotNull String module) {
    return ScalaConsoleInfo.getConsole(project) != null
        && ExecutionManagerImpl.getAllDescriptors(project).stream().anyMatch(d -> d.getDisplayName().contains("REPL")
        && d.getDisplayName().contains(module));
  }
}
