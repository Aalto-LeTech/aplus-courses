package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RunActionListener extends IdeActionListener implements ExecutionListener {

  private final String fileName;

  /**
   * Constructor.
   */
  public RunActionListener(@NotNull ListenerCallback callback, @NotNull Project project,
                           @Nullable String fileName) {
    super(callback, project, "Run");
    this.fileName = fileName;
  }

  /**
   * Creates an instance of ClassDeclarationListener based on the provided arguments.
   */
  public static RunActionListener create(ListenerCallback callback,
                                         Project project,
                                         Arguments arguments) {
    return new RunActionListener(callback, project,
        arguments.getString("filePath"));
  }

  @Override
  protected void subscribeTopics(MessageBusConnection messageBusConnection) {
    super.subscribeTopics(messageBusConnection);
    messageBusConnection.subscribe(ExecutionManager.EXECUTION_TOPIC, this);
  }

  @Override
  public void processStartScheduled(@NotNull String executorId, @NotNull ExecutionEnvironment env) {
    if (actionName.equals(executorId) && env.getRunnerAndConfigurationSettings() != null
        && env.getRunnerAndConfigurationSettings().getConfiguration().getName().equals(fileName)) {
      check(true);
    }
  }
}
