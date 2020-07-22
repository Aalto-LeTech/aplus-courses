package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertTrue;

import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;

public class APlusAuthenticationTest {

  private static final String AUTHORIZATION_HEADER = "Authorization";

  @Test
  public void testAPlusAuthentication() {
    Authentication authentication = new APlusAuthentication(new char[] {'a', 'b', 'c'});

    HttpRequest request = new HttpGet("https://example.com");
    authentication.addToRequest(request);

    Assert.assertEquals("The token should be added to the given request",
        "Token abc", request.getFirstHeader(AUTHORIZATION_HEADER).getValue());
  }

  @Test
  public void testMakesCopyOfArray() {
    char[] token = (new char[] {'d', 'e', 'f'});

    Authentication authentication = new APlusAuthentication(token);

    token[0] = 'g';
    HttpRequest request = new HttpGet("https://example.org");
    authentication.addToRequest(request);

    Assert.assertEquals("The constructor makes a copy of the given array",
        "Token def", request.getFirstHeader(AUTHORIZATION_HEADER).getValue());
  }

  @Test
  public void testClear() {
    APlusAuthentication authentication = new APlusAuthentication(new char[] {'g', 'h', 'i'});

    authentication.clear();

    assertTrue(authentication.tokenEquals("\0\0\0"));
  }

}
