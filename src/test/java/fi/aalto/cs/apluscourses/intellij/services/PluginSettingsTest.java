package fi.aalto.cs.apluscourses.intellij.services;

import static fi.aalto.cs.apluscourses.intellij.services.PluginSettings.LocalIdeSettingsNames.A_PLUS_IMPORTED_IDE_SETTINGS;
import static fi.aalto.cs.apluscourses.intellij.services.PluginSettings.LocalIdeSettingsNames.A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Test;

public class PluginSettingsTest extends BasePlatformTestCase {

  @Test
  public void testInitializeLocalIdeSettings() {
    // given
    PluginSettings.getInstance().unsetLocalIdeSettings();

    //  when
    PluginSettings.getInstance().initializeLocalIdeSettings();

    //  then
    assertTrue("The REPL dialog setting should be set to 'true'",
        PropertiesComponent.getInstance()
            .getBoolean(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.getName()));
    assertEquals("The last imported ide settings should be an empty string",
        "", PropertiesComponent.getInstance().getValue(A_PLUS_IMPORTED_IDE_SETTINGS.getName()));
  }

  @Test
  public void testResetLocalIdeSettings() {
    // given
    PluginSettings.getInstance().setShowReplConfigurationDialog(false);
    PluginSettings.getInstance().setImportedIdeSettingsId("this is not an empty string");

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
  public void testUnsetLocalIdeSettings() {
    //  given
    PluginSettings.getInstance().initializeLocalIdeSettings();

    //  when
    PluginSettings.getInstance().unsetLocalIdeSettings();

    //  then
    assertNull(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.getName() + " is successfully removed.",
        PropertiesComponent.getInstance().getValue(
            A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG.getName()));
    assertNull(A_PLUS_IMPORTED_IDE_SETTINGS.getName() + " is successfully removed.",
        PropertiesComponent.getInstance().getValue(
            A_PLUS_IMPORTED_IDE_SETTINGS.getName()));
  }

  @Test
  public void testIgnoreFileInProject() {
    String fileName = ".sampleFileToIgnore";
    String expected = FileTypeManager.getInstance().getIgnoredFilesList() + fileName + ";";

    PluginSettings.ignoreFileInProjectView(fileName, getProject());

    String actual = FileTypeManager.getInstance().getIgnoredFilesList();
    assertEquals("The file is successfully added to the ignored files list.", expected, actual);
  }
}
