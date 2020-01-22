package fi.aalto.cs.intellij.notifications;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import fi.aalto.cs.intellij.common.Version;
import org.junit.Test;

public class BetaVersionWarningTest {

  @Test
  public void testBetaVersionWarning() {
    BetaVersionWarning notification = new BetaVersionWarning(new Version(0, 22, 315));

    assertEquals("0.22.315", notification.getVersion().toString());
    assertEquals("A+", notification.getGroupId());
    assertEquals("A+ Courses plugin is under development", notification.getTitle());
    assertThat(notification.getContent(), containsString("0.22.315"));
  }
}
