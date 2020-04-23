package fi.aalto.cs.apluscourses.intellij.services;

import static fi.aalto.cs.apluscourses.intellij.services.PluginSettings.LocalSettingsNames.A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Test;

public class PluginSettingsTest extends BasePlatformTestCase {

  @Test
  public void testInitiateLocalSettingShowReplConfigurationDialogWorks() {
    //  given
    PropertiesComponent.getInstance()
        .unsetValue(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG);

    assertFalse("The state of the given setting is now 'unset'.",
        PropertiesComponent.getInstance().isValueSet(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG));

    //  when
    PluginSettings.initiateLocalSettingShowReplConfigurationDialog();

    //  then
    assertTrue("The state of the given setting is now set (to 'true').",
        PropertiesComponent.getInstance().getBoolean(A_PLUS_SHOW_REPL_CONFIGURATION_DIALOG));
  }
}