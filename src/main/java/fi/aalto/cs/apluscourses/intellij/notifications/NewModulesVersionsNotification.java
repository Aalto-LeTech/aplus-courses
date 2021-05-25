package fi.aalto.cs.apluscourses.intellij.notifications;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.Module;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link Notification} wrapper to let the user know about some A+ Course modules having the new
 * versions of them in A+ LMS.
 */
public class NewModulesVersionsNotification extends Notification {

  /**
   * Builds the notification.
   *
   * @param modules is a {@link List} {@link Module}s to process.
   */
  public NewModulesVersionsNotification(@NotNull List<Module> modules) {
    super(
        PluginSettings.A_PLUS,
        getText(modules.size() == 1
                ? "notification.NewModulesVersionsNotification.titleSingle"
                : "notification.NewModulesVersionsNotification.title"),
        getAndReplaceText(modules.size() == 1
                ? "notification.NewModulesVersionsNotification.contentSingle"
                : "notification.NewModulesVersionsNotification.content",
            getModuleNameStrings(modules)),
        NotificationType.INFORMATION);
  }

  /**
   * A naive helper method to get a comma separated String of Module names.
   *
   * @param modules a {@link List} {@link Module}s to process.
   * @return a {@link String} of comma separated Module names.
   */
  @NotNull
  public static String getModuleNameStrings(@NotNull List<Module> modules) {
    return modules
        .stream()
        .map(Component::getName)
        .collect(Collectors.joining(", "));
  }
}
