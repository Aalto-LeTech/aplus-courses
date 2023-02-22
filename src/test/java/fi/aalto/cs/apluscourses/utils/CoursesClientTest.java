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
import org.hamcrest.MatcherAssert;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CoursesClientTest {

  private HttpResponse response;

  /**
   * Set up mock objects before each test.
   */
  @BeforeEach
  void setUp() {
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
  void testRequireSuccessStatusCode() throws IOException {
    Exception exception = getRequireSuccessStatusCodeException(response);

    Assertions.assertNotNull(exception);
    MatcherAssert.assertThat("The exception message contains the status code", exception.getMessage(),
        containsString("401"));
    MatcherAssert.assertThat("The exception message contains the reason phrase", exception.getMessage(),
        containsString("test reason"));
  }

  @Test
  void testRequireSuccessStatusCodeWithResponseBody1() throws IOException {
    InputStream inputStream = new ByteArrayInputStream(
        "{\"detail\":\"detailed message\",\"errors\":[\"hmm\",\"hello\"]}".getBytes()
    );
    HttpEntity entity = mock(HttpEntity.class);
    doReturn(inputStream).when(entity).getContent();
    doReturn(entity).when(response).getEntity();

    Exception exception = getRequireSuccessStatusCodeException(response);

    Assertions.assertNotNull(exception);
    MatcherAssert.assertThat("The exception message contains the detail string", exception.getMessage(),
        containsString("detailed message"));
  }

  @Test
  void testRequireSuccessStatusCodeWithResponseBody2() throws IOException {
    InputStream inputStream = new ByteArrayInputStream(
        "{\"detail\":\"   \",\"errors\":[\"hmm\",\"hello\"]}".getBytes()
    );
    HttpEntity entity = mock(HttpEntity.class);
    doReturn(inputStream).when(entity).getContent();
    doReturn(entity).when(response).getEntity();

    Exception exception = getRequireSuccessStatusCodeException(response);

    Assertions.assertNotNull(exception);
    MatcherAssert.assertThat("The exception message contains the errors", exception.getMessage(),
        containsString("hmm"));
    MatcherAssert.assertThat("The exception message contains the errors", exception.getMessage(),
        containsString("hello"));
  }

  @Test
  void testRequireSuccessStatusCodeWithMalformedResponseBody() throws IOException {
    InputStream inputStream = new ByteArrayInputStream("{}".getBytes());
    HttpEntity entity = mock(HttpEntity.class);
    doReturn(inputStream).when(entity).getContent();
    doReturn(entity).when(response).getEntity();

    Exception exception = getRequireSuccessStatusCodeException(response);

    Assertions.assertNotNull(exception);
    MatcherAssert.assertThat("The exception message contains the status code", exception.getMessage(),
        containsString("401"));
    MatcherAssert.assertThat("The exception message contains the reason phrase", exception.getMessage(),
        containsString("test reason"));
  }

}

