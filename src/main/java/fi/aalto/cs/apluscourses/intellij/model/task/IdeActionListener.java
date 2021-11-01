package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.annotations.RequiresEdt;
import com.intellij.util.messages.MessageBusConnection;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;

public abstract class IdeActionListener extends ActivitiesListenerBase<Boolean> implements AnActionListener {

  protected final Project project;
  protected final String actionName;

  private MessageBusConnection messageBusConnection;

  /**
   * Constructor.
   */
  protected IdeActionListener(ListenerCallback callback, Project project,
                              String actionName) {
    super(callback);
    this.project = project;
    this.actionName = actionName;
  }

  @RequiresEdt
  @Override
  protected void registerListenerOverride() {
    messageBusConnection = project.getMessageBus().connect();
    subscribeTopics(messageBusConnection);
  }

  protected void subscribeTopics(MessageBusConnection messageBusConnection) {
    messageBusConnection.subscribe(AnActionListener.TOPIC, this);
  }

  @RequiresEdt
  @Override
  protected void unregisterListenerOverride() {
    messageBusConnection.disconnect();
    messageBusConnection = null;
  }

  @Override
  protected Boolean getDefaultParameter() {
    return false;
  }

  @Override
  protected boolean checkOverride(Boolean param) {
    return param;
  }
}
