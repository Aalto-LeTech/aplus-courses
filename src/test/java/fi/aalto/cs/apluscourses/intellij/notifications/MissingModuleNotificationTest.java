package fi.aalto.cs.apluscourses.intellij.notifications;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;

public class MissingModuleNotificationTest {

  @Test
  public void testMissingModuleNotification() {
    String name = "module name";
    MissingModuleNotification notification = new MissingModuleNotification(name);
    Assert.assertEquals("The title is correct", "Could not find module",
        notification.getTitle());
    Assert.assertEquals("Module name should be correct", name, notification.getModuleName());
    Assert.assertEquals("Group ID should be A+", "A+", notification.getGroupId());
    Assert.assertThat("The content should contain the module name", notification.getContent(),
        containsString(name));
  }

}
