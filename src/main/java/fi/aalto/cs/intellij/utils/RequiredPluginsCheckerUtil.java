package fi.aalto.cs.intellij.utils;

import static java.util.stream.Collectors.toMap;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.plugins.RepositoryHelper;
import com.intellij.ide.plugins.newui.BgProgressIndicator;
import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.extensions.PluginId;
import fi.aalto.cs.intellij.activities.RequiredPluginsCheckerActivity;
import fi.aalto.cs.intellij.notifications.FailedToLoadPluginsNotification;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util class containing support methods for {@link RequiredPluginsCheckerActivity}.
 */
public class RequiredPluginsCheckerUtil {

  private static final Logger logger = LoggerFactory
      .getLogger(RequiredPluginsCheckerUtil.class);

  /**
   * Filters out the plugin names that are missing from the current installation.
   *
   * @param requiredPlugins is a {@link Map} of {@link String}s for required plugins, with
   *                        plugin id as key and plugin name as value.
   * @return a {@link Map} of {@link String}s for missing plugins, where key is a plugin's name and
   *                            value is plugin's id.
   */
  @NotNull
  public static Map<String, String> filterMissingOrDisabledPluginNames(
      @NotNull Map<String, String> requiredPlugins) {
    return requiredPlugins
        .entrySet()
        .stream()
        .filter(RequiredPluginsCheckerUtil::isEntryContentsNonNull)
        .filter(entry -> isPluginMissingOrDisabled(entry.getKey()))
        .collect(toMap(Entry::getKey, Entry::getValue));
  }

  /**
   * A simple method to check if the Map entry is valid.
   *
   * @param entry to perform overall null checks on.
   * @return boolean that represents entry status, false if either key or value is 'null'.
   */
  public static boolean isEntryContentsNonNull(@NotNull Entry entry) {
    return entry.getKey() != null && entry.getValue() != null;
  }

  /**
   * Predicate for checking the plugin status against IJ framework.
   *
   * @param id is a {@link String} representation of a plugin id.
   * @return true if the plugin is missing or disabled (but installed).
   */
  public static boolean isPluginMissingOrDisabled(@NotNull String id) {
    PluginId pluginId = PluginId.getId(id);
    return !PluginManager.isPluginInstalled(pluginId)
        || !PluginManager.getPlugin(pluginId).isEnabled();
  }

  /**
   * If there are any plugins missing, creates a list of the plugin descriptors for them based on
   * the publicly available ones.
   *
   * <p>Note! This won't work if the required plugins aren't published in JB's main plugin repo.
   *
   * @param missingOrDisabledPlugins is a {@link Map} of {@link String}s for missing plugins,
   *                                 where key is a plugin's id and value is plugin's name.
   * @return a {@link List} of {@link IdeaPluginDescriptor} corresponding to the input.
   */
  @NotNull
  public static List<IdeaPluginDescriptor> createListOfMissingOrDisabledPluginDescriptors(
      @NotNull Map<String, String> missingOrDisabledPlugins) {
    List<IdeaPluginDescriptor> missingOrDisabledIdeaPluginDescriptors = new ArrayList<>();
    if (!missingOrDisabledPlugins.isEmpty()) {
      List<IdeaPluginDescriptor> availableIdeaPluginDescriptors = getAvailablePluginsFromMainRepo();
      missingOrDisabledPlugins.forEach((id, name) ->
          availableIdeaPluginDescriptors
          .stream()
          .filter(Objects::nonNull)
          .filter(
              availableDescriptor -> availableDescriptor.getPluginId().equals(PluginId.getId(id))
          )
          .findFirst()
          .ifPresent(missingOrDisabledIdeaPluginDescriptors::add));
    }
    return missingOrDisabledIdeaPluginDescriptors;
  }

  /**
   * Get the list of all the available in the main JetBrains' plugin repository.
   *
   * <p>Note! The descriptors for the not installed plugins do not exist in IJ until now.
   *
   * @return a {@link List} of available {@link IdeaPluginDescriptor}s representing plugins.
   */
  @NotNull
  public static List<IdeaPluginDescriptor> getAvailablePluginsFromMainRepo() {
    List<IdeaPluginDescriptor> availableIdeaPluginDescriptors = new ArrayList<>();
    try {
      availableIdeaPluginDescriptors = RepositoryHelper.loadPlugins(new BgProgressIndicator());
    } catch (IOException ex) {
      logger.error("Could not retrieve plugins data from the main repository.", ex);
      Notification notification = new FailedToLoadPluginsNotification();
      Notifications.Bus.notify(notification);
    }
    return availableIdeaPluginDescriptors;
  }

  /**
   * Filter out disabled plugins from the missing and disabled plugins list.
   *
   * @param missingOrDisabledIdeaPluginDescriptors list of required plugins that are not present or
   *                                               disabled.
   * @return a cleared from disabled {@link List} of {@link IdeaPluginDescriptor}s.
   */
  @NotNull
  public static List<IdeaPluginDescriptor> filterDisabledPluginDescriptors(@NotNull
      List<IdeaPluginDescriptor> missingOrDisabledIdeaPluginDescriptors) {
    return missingOrDisabledIdeaPluginDescriptors
        .stream()
        .filter(Objects::nonNull)
        .filter(descriptor -> PluginManager.isDisabled(descriptor.getPluginId().getIdString()))
        .collect(Collectors.toList());
  }

  /**
   * Filter out missing plugins from the missing and disabled plugins list.
   *
   * @param missingOrDisabledIdeaPluginDescriptors list of required plugins that are not present or
   *                                               disabled.
   * @return a cleared from missing {@link List} of {@link IdeaPluginDescriptor}s.
   */
  @NotNull
  public static List<IdeaPluginDescriptor> filterMissingPluginDescriptors(@NotNull
      List<IdeaPluginDescriptor> missingOrDisabledIdeaPluginDescriptors) {
    return missingOrDisabledIdeaPluginDescriptors
        .stream()
        .filter(Objects::nonNull)
        .filter(descriptor -> !PluginManager.isPluginInstalled(descriptor.getPluginId()))
        .collect(Collectors.toList());
  }

  /**
   * Join plugin descriptor names with a comma.
   *
   * @return pretty-formatted {@link String} of plugin names.
   */
  @NotNull
  public static String getPluginsNamesString(@NotNull List<IdeaPluginDescriptor> descriptors) {
    return descriptors
        .stream()
        .filter(Objects::nonNull)
        .map(IdeaPluginDescriptor::getName)
        .collect(Collectors.joining(", "));
  }
}
