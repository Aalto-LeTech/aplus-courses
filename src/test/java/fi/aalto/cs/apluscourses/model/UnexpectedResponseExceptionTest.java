package fi.aalto.cs.apluscourses.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertSame;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.junit.Test;

public class UnexpectedResponseExceptionTest {

  @Test
  public void testCreateUnexpectedResponseExceptionTest() {
    DefaultHttpResponseFactory factory = new DefaultHttpResponseFactory();
    HttpResponse response = factory.newHttpResponse(HttpVersion.HTTP_1_1, 200, null);
    String message = "My awesome test message";
    UnexpectedResponseException exception
        = new UnexpectedResponseException(response, message);

    assertSame("Response should be the one given to the constructor", response,
        exception.getResponse());
    assertThat("The message should contain the message given to the constructor",
        exception.getMessage(), containsString(message));
  }
}
