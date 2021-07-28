package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class IdeActionListener implements AnActionListener, ActivitiesListener {
  
  protected final Project project;
  protected MessageBusConnection messageBusConnection;
  protected final List<String> actionNames;
  protected final ListenerCallback callback;

  /**
   * Constructor.
   */
  protected IdeActionListener(@NotNull ListenerCallback callback,
                              @NotNull Project project,
                              @NotNull String[] actionNames) {
    this.callback = callback;
    this.project = project;
    this.actionNames = Arrays.asList(actionNames);
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

  public static IdeActionListener create(ListenerCallback callback, Project project,
                                         Arguments arguments) {
    return new IdeActionListener(callback, project, arguments.getArray("actionNames"));
  }

  @Override
  public void beforeActionPerformed(@NotNull AnAction action, @NotNull DataContext dataContext,
                                    @NotNull AnActionEvent event) {
    if ((actionNames.contains(action.getTemplateText()))
                    || actionNames.contains(event.getPresentation().getText())) {
      ApplicationManager.getApplication().invokeLater(callback::callback);
    }
  }

}
