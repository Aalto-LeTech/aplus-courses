package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import org.jetbrains.annotations.NotNull;

public class StopListener extends IdeActionListener implements ExecutionListener {
  private final String appName;


  protected StopListener(ListenerCallback callback, Project project, String appName) {
    super(callback, project, "Stop");
    this.appName = appName;
  }

  public static ActivitiesListener create(ListenerCallback callback, Project project, Arguments arguments) {
    return new StopListener(callback, project, arguments.getString("appName"));
  }

  @Override
  protected void subscribeTopics(MessageBusConnection messageBusConnection) {
    super.subscribeTopics(messageBusConnection);
    messageBusConnection.subscribe(ExecutionManager.EXECUTION_TOPIC, this);
  }

  @Override
  public void processTerminated(@NotNull String executorId,
                                @NotNull ExecutionEnvironment env,
                                @NotNull ProcessHandler handler,
                                int exitCode) {
    if (env.getRunnerAndConfigurationSettings() != null
        && appName.equals(env.getRunnerAndConfigurationSettings().getConfiguration().getName())) {
      check(true);
    }
  }
}
