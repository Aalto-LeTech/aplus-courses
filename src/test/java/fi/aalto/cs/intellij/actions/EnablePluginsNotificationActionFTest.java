package fi.aalto.cs.intellij.actions;

import fi.aalto.cs.intellij.PluginsTestHelper;
import org.junit.Test;

public class EnablePluginsNotificationActionFTest extends PluginsTestHelper {

  @Test
  public void testProposeRestart() {
    assertThrows(
        RuntimeException.class,
        "Restart IntelliJ IDEA to apply changes in plugins?",
        EnablePluginsNotificationAction::proposeRestart
    );
  }
}
