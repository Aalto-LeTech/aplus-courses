package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class IoErrorNotificationTest {

  @Test
  public void testIoErrorNotification() {
    IOException exception = new IOException("hello there");
    IoErrorNotification notification = new IoErrorNotification(exception);
    Assert.assertEquals("The notification has the correct group ID",
        "A+", notification.getGroupId());
    Assert.assertThat("The exception message is in the notification content",
        notification.getContent(), containsString("hello there"));
  }

}
