package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.ide.plugins.DynamicPluginListener;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import org.jetbrains.annotations.NotNull;

public class PluginUnloadListener implements DynamicPluginListener {

  @Override
  public void beforePluginUnload(@NotNull IdeaPluginDescriptor pluginDescriptor, boolean isUpdate) {
    System.out.println("deleting plugin...");

    PluginSettings.getInstance().unsetLocalSettings();
  }
}
