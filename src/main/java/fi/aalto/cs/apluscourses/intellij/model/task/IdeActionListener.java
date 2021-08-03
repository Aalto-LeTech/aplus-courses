package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;

public abstract class IdeActionListener implements AnActionListener, ActivitiesListener {
  
  protected final Project project;
  protected MessageBusConnection messageBusConnection;
  protected final String actionName;
  protected final ListenerCallback callback;

  /**
   * Constructor.
   */
  protected IdeActionListener(ListenerCallback callback, Project project,
                           String actionName) {
    this.callback = callback;
    this.project = project;
    this.actionName = actionName;
  }

  @Override
  public boolean registerListener() {
    messageBusConnection = project.getMessageBus().connect();
    messageBusConnection.subscribe(AnActionListener.TOPIC, this);
    return false;
  }

  @Override
  public void unregisterListener() {
    if (messageBusConnection != null) {
      messageBusConnection.disconnect();
      messageBusConnection = null;
    }
  }
}
