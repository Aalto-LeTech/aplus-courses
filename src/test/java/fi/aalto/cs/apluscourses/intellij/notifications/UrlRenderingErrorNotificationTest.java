package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;

import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UrlRenderingErrorNotificationTest {

  @Test
  void testSubmissionRenderingErrorNotification() {
    Exception exception = new Exception("hello there");
    UrlRenderingErrorNotification notification
        = new UrlRenderingErrorNotification(exception);
    Assertions.assertEquals("Failed to open item", notification.getTitle(), "The title is correct");
    Assertions.assertSame(exception, notification.getException(), "The exception is the one given to the constructor");
    Assertions.assertEquals("A+", notification.getGroupId(), "Group ID should be A+");
    MatcherAssert.assertThat("The content should contain the exception message", notification.getContent(),
        containsString(exception.getMessage()));
  }

}
