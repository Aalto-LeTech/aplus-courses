package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import com.intellij.util.messages.MessageBusConnection;
import fi.aalto.cs.apluscourses.model.Task;

public interface ActivitiesListener {

  static void createListener(Task task, Project project) {
    ActivitiesListener listener = ActivitiesListenerFactory.createListener(task);
    if (listener != null) {
      listener.registerListener(project);
      task.setListener(listener);
    }
  }

  void registerListener(Project project);

  void unregisterListener();

  @RequiresReadLock
  default void unregisterListener(MessageBusConnection messageBusConnection) {
    if (messageBusConnection != null) {
      messageBusConnection.disconnect();
    }
  }
}