package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class TaskNotifier {

  private final @NotNull Notifier notifier;
  private final @NotNull Project project;

  public TaskNotifier(@NotNull Notifier notifier, @NotNull Project project) {
    this.notifier = notifier;
    this.project = project;
  }

  public void notifyAlreadyEndTask(int index, String instructions) {
    notifier.notify(TaskCompleteNotification.createTaskAlreadyCompleteNotification(index, instructions), project);
  }

  public void notifyEndTask(int index) {
    notifier.notify(TaskCompleteNotification.createTaskCompleteNotification(index), project);
  }
}