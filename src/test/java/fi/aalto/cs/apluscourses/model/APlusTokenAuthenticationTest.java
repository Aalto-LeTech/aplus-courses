package fi.aalto.cs.apluscourses.model;

import static fi.aalto.cs.apluscourses.dal.APlusTokenAuthentication.AUTHORIZATION_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import fi.aalto.cs.apluscourses.dal.APlusTokenAuthentication;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Test;

public class APlusTokenAuthenticationTest {

  @Test
  public void testAPlusAuthentication() {
    Authentication authentication = new APlusTokenAuthentication(new char[] {'a', 'b', 'c'});

    HttpRequest request = new HttpGet("https://example.com");
    authentication.addToRequest(request);

    assertEquals("The token should be added to the given request",
        "Token abc", request.getFirstHeader(AUTHORIZATION_HEADER).getValue());
  }

  @Test
  public void testCreationCopiesToken() {
    char[] token = (new char[] {'d', 'e', 'f'});

    Authentication authentication = new APlusTokenAuthentication(token);

    token[0] = 'g';
    HttpRequest request = new HttpGet("https://example.org");
    authentication.addToRequest(request);

    assertEquals("The constructor makes a copy of the given array.",
        "Token def", request.getFirstHeader(AUTHORIZATION_HEADER).getValue());
  }

  @Test
  public void testClearToken() {
    char[] token = new char[] {'g', 'h', 'i'};
    APlusTokenAuthentication authentication = new APlusTokenAuthentication(token);

    authentication.clear();

    HttpRequest request = new HttpGet("https://example.org");
    authentication.addToRequest(request);

    assertEquals("Token \0\0\0", request.getFirstHeader(AUTHORIZATION_HEADER).getValue());
  }
}
