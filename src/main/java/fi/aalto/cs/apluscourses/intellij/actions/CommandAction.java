package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import fi.aalto.cs.apluscourses.intellij.io.IntelliJDialogs;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.commands.Command;
import fi.aalto.cs.apluscourses.presentation.commands.MainViewModelContext;
import fi.aalto.cs.apluscourses.presentation.dialogs.Dialogs;
import fi.aalto.cs.apluscourses.presentation.messages.Message;
import fi.aalto.cs.apluscourses.presentation.messages.Messenger;
import org.jetbrains.annotations.NotNull;

public abstract class CommandAction<T extends Command.Context>
    extends AnAction {

  @NotNull
  private final Command<T> command;

  protected CommandAction(@NotNull Command<T> command) {
    this.command = command;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setEnabled(command.canExecute(getContext(e)));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    command.execute(getContext(e));
  }

  @NotNull
  protected abstract T getContext(@NotNull AnActionEvent e);

  /**
   * Returns a {@link NotificationType} that corresponds to the given level.
   * @param level A level.
   * @return A notification type.
   */
  public static NotificationType getNotificationType(Message.Level level) {
    switch (level) {
      case ERR:
        return NotificationType.ERROR;
      case WARN:
        return NotificationType.WARNING;
      case INFO:
        return NotificationType.INFORMATION;
      default:
        throw new IllegalArgumentException("Level is not valid.");
    }
  }

  public static Notification getNotificationFor(Message message) {
    return new Notification("A+", message.getTitle(), message.getContent(),
        getNotificationType(message.getLevel()));
  }

  protected static class BaseContext implements Command.Context {

    protected AnActionEvent event;

    protected BaseContext(AnActionEvent event) {
      this.event = event;
    }

    @Override
    public Dialogs getDialogs() {
      return object -> IntelliJDialogs.DEFAULT.create(object, event.getProject());
    }

    @Override
    public Messenger getMessenger() {
      return message -> Notifications.Bus.notify(getNotificationFor(message), event.getProject());
    }
  }

  protected static class MainViewModelContextImpl extends BaseContext
      implements MainViewModelContext {

    protected MainViewModelContextImpl(AnActionEvent event) {
      super(event);
    }

    @NotNull
    @Override
    public MainViewModel getMainViewModel() {
      return PluginSettings.getInstance().getMainViewModel(event.getProject());
    }
  }
}
