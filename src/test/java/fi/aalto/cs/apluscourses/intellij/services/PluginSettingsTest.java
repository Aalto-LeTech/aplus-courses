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
    PluginSettings.getInstance().unsetLocalSettings();

    //  when
    PluginSettings.getInstance().initializeLocalSettings();

    //  then
    assertTrue("The REPL dialog setting should be set to 'true'",
        PropertiesComponent.getInstance()
            .getBoolean(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.getName()));
    assertEquals("The last imported ide settings should be an empty string",
        "", PropertiesComponent.getInstance().getValue(A_PLUS_IMPORTED_IDE_SETTINGS.getName()));
  }

  @Test
  public void testResetLocalSettings() {
    // given
    PluginSettings.getInstance().setShowReplConfigurationDialog(false);
    PluginSettings.getInstance().setImportedIdeSettingsName("this is not an empty string");

    // when
    PluginSettings.getInstance().resetLocalSettings();

    // then
    assertTrue("The REPL dialog setting should be set to 'true'",
        PropertiesComponent.getInstance()
            .getBoolean(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.getName()));
    assertEquals("The last imported ide settings should be an empty string",
        "", PropertiesComponent.getInstance().getValue(A_PLUS_IMPORTED_IDE_SETTINGS.getName()));
  }

  @Test
  public void testUnsetLocalSettings() {
    //  given
    PluginSettings.getInstance().initializeLocalSettings();

    //  when
    PluginSettings.getInstance().unsetLocalSettings();

    //  then
    assertNull(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.getName() + " is successfully removed.",
        PropertiesComponent.getInstance().getValue(
            A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.getName()));
    assertNull(A_PLUS_IMPORTED_IDE_SETTINGS.getName() + " is successfully removed.",
        PropertiesComponent.getInstance().getValue(
            A_PLUS_IMPORTED_IDE_SETTINGS.getName()));
  }

}
