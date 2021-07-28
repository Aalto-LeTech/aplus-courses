package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class TaskNotifier {

  private final @NotNull Notifier notifier;
  private final @NotNull Project project;

  public TaskNotifier(@NotNull  Notifier notifier, @NotNull Project project) {
    this.notifier = notifier;
    this.project = project;
  }

  public void notifyAlreadyEndTask(int index, String instructions) {
    notifier.notifyAndHide(TaskCompleteNotification.createTaskAlreadyCompleteNotification(index, instructions),
        project);
  }

  public void notifyEndTask(int index) {
    notifier.notifyAndHide(TaskCompleteNotification.createTaskCompleteNotification(index), project);
  }

  public void notifyDownloadingDeps(boolean done) {
    notifier.notifyAndHide(new DownloadingDependenciesNotification(done), project);
  }

  public void notifyMissingModule(@NotNull String moduleName) {
    notifier.notifyAndHide(new MissingModuleNotification(moduleName), project);
  }
}