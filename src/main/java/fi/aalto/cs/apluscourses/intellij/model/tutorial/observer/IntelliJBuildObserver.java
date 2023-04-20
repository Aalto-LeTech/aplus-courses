package fi.aalto.cs.apluscourses.intellij.model.tutorial.observer;

import com.intellij.compiler.server.BuildManagerListener;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class IntelliJBuildObserver extends IntelliJMessageBusObserverBase<BuildManagerListener> {

  private final @NotNull BuildManagerListener listener;

  public IntelliJBuildObserver(@NotNull String action, @NotNull TutorialComponent component) {
    super(BuildManagerListener.TOPIC, component);
    listener = chooseListener(action);
  }

  private void onAction(@NotNull Project project) {
    if (project.equals(getProject())) {
      fire();
    }
  }

  protected @NotNull BuildManagerListener chooseListener(@NotNull String action) {
    switch (action) {
      case BUILD_START:
        return new BuildStartedListener();
      case BUILD_FINISH:
        return new BuildFinishedListener();
      default:
        throw new IllegalArgumentException("Unknown build action: " + action);
    }
  }

  @Override
  protected @NotNull BuildManagerListener getMessageListener() {
    return listener;
  }

  private class BuildFinishedListener implements BuildManagerListener {
    @Override
    public void buildFinished(@NotNull Project project, @NotNull UUID sessionId, boolean isAutomake) {
      onAction(project);
    }
  }

  private class BuildStartedListener implements BuildManagerListener {
    @Override
    public void buildStarted(@NotNull Project project, @NotNull UUID sessionId, boolean isAutomake) {
      onAction(project);
    }
  }
}
