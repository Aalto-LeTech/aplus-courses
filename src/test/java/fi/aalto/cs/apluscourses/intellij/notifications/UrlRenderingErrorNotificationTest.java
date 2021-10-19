package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class UrlRenderingErrorNotificationTest {

  @Test
  public void testSubmissionRenderingErrorNotification() {
    Exception exception = new Exception("hello there");
    UrlRenderingErrorNotification notification
        = new UrlRenderingErrorNotification(exception);
    Assert.assertEquals("The title is correct",
        "Failed to open item", notification.getTitle());
    Assert.assertSame("The exception is the one given to the constructor",
        exception, notification.getException());
    Assert.assertEquals("Group ID should be A+", "A+", notification.getGroupId());
    Assert.assertThat("The content should contain the exception message",
        notification.getContent(), containsString(exception.getMessage()));
  }

}
