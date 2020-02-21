package fi.aalto.cs.apluscourses.intellij.notifications;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class ClientIoErrorTest {

  @Test
  public void testClientIoError() {
    Exception exception = new Exception();
    ClientIoError notification = new ClientIoError(exception);

    assertEquals("Group ID should be 'A+'",
        "A+", notification.getGroupId());
    assertEquals("Title should be 'A+ Courses encountered a network error'",
        "A+ Courses plugin encountered a network error",
        notification.getTitle());
    assertSame("Exception should be same as the one that was given to the constructor",
        exception, notification.getException());
  }

}

