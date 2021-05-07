package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import org.jetbrains.annotations.NotNull;

public class IdeActionListener implements AnActionListener, ActivitiesListener {
  private final Project project;
  private MessageBusConnection messageBusConnection;
  private final String actionName;
  private final ListenerCallback callback;

  /**
   * Constructor.
   * @param callback
   * @param project
   * @param action
   */
  public IdeActionListener(ListenerCallback callback, Project project, String action) {
    this.callback = callback;
    this.project = project;
    this.actionName = action;
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

  @Override
  public void beforeActionPerformed(@NotNull AnAction action, @NotNull DataContext dataContext,
                                    @NotNull AnActionEvent event) {

    System.out.println("Action: " + action.getTemplateText()
            + " ActionEvent: " + event.getPlace());
    if (actionName.equals(action.getTemplateText())) {
      callback.callback();
    }
  }
}
