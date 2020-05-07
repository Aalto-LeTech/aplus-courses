package fi.aalto.cs.apluscourses.intellij.services;

import static fi.aalto.cs.apluscourses.intellij.services.PluginSettings.LocalSettingsNames.A_PLUS_IMPORTED_IDE_SETTINGS;
import static fi.aalto.cs.apluscourses.intellij.services.PluginSettings.LocalSettingsNames.A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Test;

public class PluginSettingsTest extends BasePlatformTestCase {

  @Test
  public void testInitializeLocalSettings() {
    // given
    PropertiesComponent.getInstance()
        .unsetValue(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG);
    PropertiesComponent.getInstance()
        .unsetValue(A_PLUS_IMPORTED_IDE_SETTINGS);

    // when
    PluginSettings.getInstance().initializeLocalSettings();

    assertTrue("The REPL configuration dialog option should be set to true",
        PropertiesComponent.getInstance().getBoolean(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG));
    assertEquals("The last imported ide settings should be an empty string",
        "", PropertiesComponent.getInstance().getValue(A_PLUS_IMPORTED_IDE_SETTINGS));
  }

}
