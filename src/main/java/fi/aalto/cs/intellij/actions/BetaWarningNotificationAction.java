package fi.aalto.cs.intellij.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BetaWarningNotificationAction extends NotificationAction {

  public BetaWarningNotificationAction(@Nls(capitalization = Nls.Capitalization.Title) @Nullable String text) {
    super(text);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
    notification.setTitle("Using beta version of A+ Courses plugin.");
  }
}
