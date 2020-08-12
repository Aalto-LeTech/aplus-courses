package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class NetworkErrorNotificationTest {

  @Test
  public void testNetworkErrorNotification() {
    Exception exception = new Exception("hello there");
    NetworkErrorNotification notification = new NetworkErrorNotification(exception);

    assertEquals("Group ID should be 'A+'", "A+", notification.getGroupId());
    assertEquals("Title should be 'A+ Courses encountered a network error'",
        "The A+ Courses plugin encountered a network error",
        notification.getTitle());
    assertThat("The content should include the exception message",
        notification.getContent(), containsString(exception.getMessage()));
  }
}

