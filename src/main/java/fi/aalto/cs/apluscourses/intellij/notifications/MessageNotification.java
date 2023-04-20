package fi.aalto.cs.apluscourses.intellij.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.presentation.base.Message;
import java.util.logging.Level;
import org.jetbrains.annotations.NotNull;

public class MessageNotification extends Notification {

  public MessageNotification(@NotNull Message message) {
    super(message.getGroupId(), message.getTitle(), message.getContent(), levelToType(message.getLevel()));
  }

  public static @NotNull NotificationType levelToType(@NotNull Level level) {
    if (level.intValue() >= Level.SEVERE.intValue()) {
      return NotificationType.ERROR;
    } else if (level.intValue() >= Level.WARNING.intValue()) {
      return NotificationType.WARNING;
    }
    return NotificationType.INFORMATION;
  }
}
