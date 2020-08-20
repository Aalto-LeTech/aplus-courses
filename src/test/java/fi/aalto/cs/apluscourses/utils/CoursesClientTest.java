package fi.aalto.cs.apluscourses.utils;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import fi.aalto.cs.apluscourses.model.UnexpectedResponseException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CoursesClientTest {

  private HttpResponse response;

  /**
   * Set up mock objects before each test.
   */
  @Before
  public void setUp() {
    StatusLine statusLine = mock(StatusLine.class);
    doReturn(401).when(statusLine).getStatusCode();
    doReturn("test reason").when(statusLine).getReasonPhrase();
    response = mock(HttpResponse.class);
    doReturn(statusLine).when(response).getStatusLine();
  }

  @NotNull
  private static Exception getRequireSuccessStatusCodeException(@NotNull HttpResponse response)
      throws IOException {
    try {
      CoursesClient.requireSuccessStatusCode(response);
      throw new IllegalStateException();
    } catch (UnexpectedResponseException e) {
      return e;
    }
  }

  @Test
  public void testRequireSuccessStatusCode() throws IOException {
    Exception exception = getRequireSuccessStatusCodeException(response);

    Assert.assertNotNull(exception);
    Assert.assertThat("The exception message contains the status code",
        exception.getMessage(), containsString("401"));
    Assert.assertThat("The exception message contains the reason phrase",
        exception.getMessage(), containsString("test reason"));
  }

  @Test
  public void testRequireSuccessStatusCodeWithResponseBody1() throws IOException {
    InputStream inputStream = new ByteArrayInputStream(
        "{\"detail\":\"detailed message\",\"errors\":[\"hmm\",\"hello\"]}".getBytes()
    );
    HttpEntity entity = mock(HttpEntity.class);
    doReturn(inputStream).when(entity).getContent();
    doReturn(entity).when(response).getEntity();

    Exception exception = getRequireSuccessStatusCodeException(response);

    Assert.assertNotNull(exception);
    Assert.assertThat("The exception message contains the detail string",
        exception.getMessage(), containsString("detailed message"));
  }

  @Test
  public void testRequireSuccessStatusCodeWithResponseBody2() throws IOException {
    InputStream inputStream = new ByteArrayInputStream(
        "{\"detail\":\"   \",\"errors\":[\"hmm\",\"hello\"]}".getBytes()
    );
    HttpEntity entity = mock(HttpEntity.class);
    doReturn(inputStream).when(entity).getContent();
    doReturn(entity).when(response).getEntity();

    Exception exception = getRequireSuccessStatusCodeException(response);

    Assert.assertNotNull(exception);
    Assert.assertThat("The exception message contains the errors",
        exception.getMessage(), containsString("hmm"));
    Assert.assertThat("The exception message contains the errors",
        exception.getMessage(), containsString("hello"));
  }

  @Test
  public void testRequireSuccessStatusCodeWithMalformedResponseBody() throws IOException {
    InputStream inputStream = new ByteArrayInputStream("{}".getBytes());
    HttpEntity entity = mock(HttpEntity.class);
    doReturn(inputStream).when(entity).getContent();
    doReturn(entity).when(response).getEntity();

    Exception exception = getRequireSuccessStatusCodeException(response);

    Assert.assertNotNull(exception);
    Assert.assertThat("The exception message contains the status code",
        exception.getMessage(), containsString("401"));
    Assert.assertThat("The exception message contains the reason phrase",
        exception.getMessage(), containsString("test reason"));
  }

}

