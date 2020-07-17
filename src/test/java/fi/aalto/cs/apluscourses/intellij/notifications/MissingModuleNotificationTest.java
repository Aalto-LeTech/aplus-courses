package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;

import org.junit.Assert;
import org.junit.Test;

public class MissingModuleNotificationTest {

  @Test
  public void testMissingModuleNotification() {
    MissingModuleNotification notification = new MissingModuleNotification("module name");
    Assert.assertEquals("Group ID should be A+", "A+", notification.getGroupId());
    Assert.assertThat("The content should contain the module name", notification.getContent(),
        containsString("module name"));
  }

}
