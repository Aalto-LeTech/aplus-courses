package fi.aalto.cs.apluscourses.intellij.services;

import static fi.aalto.cs.apluscourses.intellij.services.PluginSettings.LocalIdeSettingsNames.A_PLUS_IMPORTED_IDE_SETTINGS;
import static fi.aalto.cs.apluscourses.intellij.services.PluginSettings.LocalIdeSettingsNames.A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class PluginSettingsTest {

  @Test
  public void testInitializeLocalIdeSettings() {
    PluginSettings.PropertiesManager propertiesManager = new TestPropertiesManager();
    PluginSettings pluginSettings = new PluginSettings(propertiesManager);

    // given
    pluginSettings.unsetLocalIdeSettings();

    //  when
    pluginSettings.initializeLocalIdeSettings();

    //  then
    assertTrue("The REPL dialog setting should be set to 'true'",
        propertiesManager.getBoolean(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.getName()));
    assertEquals("The last imported ide settings should be an empty string",
        "", propertiesManager.getValue(A_PLUS_IMPORTED_IDE_SETTINGS.getName()));
  }

  @Test
  public void testResetLocalIdeSettings() {
    PluginSettings.PropertiesManager propertiesManager = new TestPropertiesManager();
    PluginSettings pluginSettings = new PluginSettings(propertiesManager);

    // given
    pluginSettings.setShowReplConfigurationDialog(false);
    pluginSettings.setImportedIdeSettingsId("this is not an empty string");

    // when
    pluginSettings.resetLocalSettings();

    // then
    assertTrue("The REPL dialog setting should be set to 'true'",
        propertiesManager.getBoolean(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.getName()));
    assertEquals("The last imported ide settings should be an empty string",
        "", propertiesManager.getValue(A_PLUS_IMPORTED_IDE_SETTINGS.getName()));
  }

  @Test
  public void testUnsetLocalIdeSettings() {
    PluginSettings.PropertiesManager propertiesManager = new TestPropertiesManager();
    PluginSettings pluginSettings = new PluginSettings(propertiesManager);

    //  given
    pluginSettings.initializeLocalIdeSettings();

    //  when
    pluginSettings.unsetLocalIdeSettings();

    //  then
    assertNull(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.getName() + " is successfully removed.",
        propertiesManager.getValue(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.getName()));
    assertNull(A_PLUS_IMPORTED_IDE_SETTINGS.getName() + " is successfully removed.",
        propertiesManager.getValue(A_PLUS_IMPORTED_IDE_SETTINGS.getName()));
  }

  private static class TestPropertiesManager implements PluginSettings.PropertiesManager {
    private final Map<String, Optional<String>> map = Collections.synchronizedMap(new HashMap<>());

    @Override
    public void setValue(@NotNull String key, @Nullable String value) {
      map.put(key, Optional.ofNullable(value));
    }

    @Override
    public @Nullable String getValue(@NotNull String key) {
      Optional<String> value = map.get(key);
      return value == null || value.isEmpty() ? null : value.get();
    }

    @Override
    public void unsetValue(@NotNull String key) {
      map.remove(key);
    }

    @Override
    public boolean isValueSet(@NotNull String key) {
      return map.containsKey(key);
    }
  }
}
