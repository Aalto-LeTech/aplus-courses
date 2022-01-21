package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NetworkErrorNotificationTest {

  @Test
  void testNetworkErrorNotification() {
    Exception exception = new Exception("hello there");
    NetworkErrorNotification notification = new NetworkErrorNotification(exception);

    Assertions.assertEquals("A+", notification.getGroupId(), "Group ID should be 'A+'");
    Assertions.assertEquals("The A+ Courses plugin encountered a network error", notification.getTitle(),
        "Title should be 'A+ Courses encountered a network error'");
    MatcherAssert.assertThat("The content should include the exception message", notification.getContent(),
        containsString(exception.getMessage()));
  }
}

