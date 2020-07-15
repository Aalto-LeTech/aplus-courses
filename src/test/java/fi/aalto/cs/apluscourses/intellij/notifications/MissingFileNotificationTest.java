package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;

import org.junit.Assert;
import org.junit.Test;

public class MissingFileNotificationTest {

  @Test
  public void testMissingFileNotification() {
    MissingFileNotification notification = new MissingFileNotification(
        "awesome module", "awesome file");
    Assert.assertEquals("Group ID should be A+", "A+", notification.getGroupId());
    Assert.assertThat("The content should contain the module name", notification.getContent(),
        containsString("awesome module"));
    Assert.assertThat("The content should contain the filename", notification.getContent(),
        containsString("awesome file"));
  }

}
