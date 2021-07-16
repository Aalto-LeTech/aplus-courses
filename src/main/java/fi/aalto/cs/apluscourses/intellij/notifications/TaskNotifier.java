package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.openapi.project.Project;

public class TaskNotifier {

  private final Notifier notifier;
  private final Project project;

  public TaskNotifier(Notifier notifier, Project project) {
    this.notifier = notifier;
    this.project = project;
  }

  public void notifyAlreadyEndTask(int index) {
    notifier.notify(TaskCompleteNotification.createTaskAlreadyCompleteNotification(index), project);
  }

  public void notifyEndTask(int index) {
    notifier.notify(TaskCompleteNotification.createTaskCompleteNotification(index), project);
  }
}