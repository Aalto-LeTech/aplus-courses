package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.utils.ScalaReplObserver;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import org.jetbrains.annotations.NotNull;

public class RunReplListener extends ActivitiesListenerBase<Boolean> implements ScalaReplObserver.Callback {
  private final ScalaReplObserver observer;

  /**
   * A constructor for a listener that listens for a REPL of a given module opening.
   */
  public RunReplListener(@NotNull ListenerCallback callback,
                         @NotNull Project project,
                         @NotNull String module) {
    super(callback);
    observer = new ScalaReplObserver(project, module, this);
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
  protected boolean checkOverride(Boolean param) {
    return param;
  }

  @Override
  protected void registerListenerOverride() {
    observer.start();
  }

  @Override
  protected void unregisterListenerOverride() {
    observer.stop();
  }

  @Override
  protected Boolean getDefaultParameter() {
    return observer.isReplOpen();
  }

  @Override
  public void onReplOpen() {
    check(true);
  }
}
