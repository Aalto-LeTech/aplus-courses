package fi.aalto.cs.apluscourses.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UnexpectedResponseExceptionTest {

  @Test
  void testCreateUnexpectedResponseExceptionTest() {
    DefaultHttpResponseFactory factory = new DefaultHttpResponseFactory();
    HttpResponse response = factory.newHttpResponse(HttpVersion.HTTP_1_1, 200, null);
    String message = "My awesome test message";
    UnexpectedResponseException exception
        = new UnexpectedResponseException(response, message);

    Assertions.assertSame(response, exception.getResponse(), "Response should be the one given to the constructor");
    assertThat("The message should contain the message given to the constructor",
        exception.getMessage(), containsString(message));
  }
}
