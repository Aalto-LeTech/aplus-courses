package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import fi.aalto.cs.apluscourses.utils.Version;
import org.junit.Test;

public class BetaVersionWarningTest {

  @Test
  public void testBetaVersionWarning() {
    Version version = new Version(0, 22, 315);
    BetaVersionWarning notification = new BetaVersionWarning(version);

    assertEquals("The version should be equal to the one given to the constructor.",
        version, notification.getVersion());
    assertEquals("Group ID should be 'A+'",
        "A+", notification.getGroupId());
    assertEquals("Title should be 'A+ Courses plugin is under development'.",
        "The A+ Courses plugin is under development", notification.getTitle());
    assertThat("Content should contain the version given to the constructor.",
        notification.getContent(), containsString(version.toString()));
  }
}
