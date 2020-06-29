package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;

import org.junit.Assert;
import org.junit.Test;

public class NetworkErrorNotificationTest {

  @Test
  public void testNetworkErrorNotification() {
    Exception exception = new Exception("hello there");
    NetworkErrorNotification notification = new NetworkErrorNotification(exception);

    Assert.assertEquals("Group ID should be 'A+'", "A+", notification.getGroupId());
    Assert.assertEquals("Title should be 'A+ Courses encountered a network error'",
        "A+ Courses plugin encountered a network error",
        notification.getTitle());
    Assert.assertThat("The content should include the exception message",
        notification.getContent(), containsString(exception.getMessage()));
  }
}

