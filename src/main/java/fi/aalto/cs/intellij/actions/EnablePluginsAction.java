package fi.aalto.cs.intellij.actions;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.extensions.PluginId;
import java.util.Objects;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnablePluginsAction extends NotificationAction {

  private List<IdeaPluginDescriptor> descriptors;

  public EnablePluginsAction(
      @Nls(capitalization = Nls.Capitalization.Title) @Nullable String text,
      @NotNull List<IdeaPluginDescriptor> descriptors) {
    super(text);
    this.descriptors = descriptors;
  }

  /**
   * Activate all the required plugins.
   */
  @Override
  public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
    descriptors.forEach(descriptor -> descriptor.setEnabled(true));
    notification.expire();
  }
}
