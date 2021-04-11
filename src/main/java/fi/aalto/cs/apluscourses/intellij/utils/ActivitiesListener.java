package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import fi.aalto.cs.apluscourses.model.Task;

public interface ActivitiesListener {

  static void createListener(Task task, Project project) {
    ActivitiesListener listener = ActivitiesListenerFactory.createListener(task);
    listener.registerListener(project);
  }

  void registerListener(Project project);

  default void unregisterListener(MessageBusConnection messageBusConnection) {
    if (messageBusConnection != null) {
      messageBusConnection.disconnect();
    }
  }
}