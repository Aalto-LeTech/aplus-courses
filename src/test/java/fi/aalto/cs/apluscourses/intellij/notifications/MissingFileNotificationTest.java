package fi.aalto.cs.apluscourses.intellij.notifications;

import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;

import static org.hamcrest.Matchers.containsString;

public class MissingFileNotificationTest {

  @Test
  public void testMissingFileNotification() {
    MissingFileNotification notification = new MissingFileNotification(
        Paths.get("awesome_module"), "awesome file");
    Assert.assertEquals("Group ID should be A+", "A+", notification.getGroupId());
    Assert.assertThat("The content should contain the path", notification.getContent(),
        containsString("awesome_module"));
    Assert.assertThat("The content should contain the filename", notification.getContent(),
        containsString("awesome file"));
  }

}
