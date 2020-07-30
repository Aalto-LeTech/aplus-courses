package fi.aalto.cs.apluscourses.presentation.messages;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import fi.aalto.cs.apluscourses.presentation.messages.NetworkErrorMessage;
import org.junit.Test;

public class NetworkErrorMessageTest {

  @Test
  public void testNetworkErrorMessage() {
    Exception exception = new Exception("hello there");
    NetworkErrorMessage message = new NetworkErrorMessage(exception);

    assertEquals("Title should be 'A+ Courses encountered a network error'",
        "A+ Courses plugin encountered a network error", message.getTitle());
    assertThat("The content should include the exception message",
        message.getContent(), containsString(exception.getMessage()));
    assertSame(exception, message.getException());
  }
}

