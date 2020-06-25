package fi.aalto.cs.apluscourses.model;

import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;

public class APlusAuthenticationTest {

  private static final String AUTHORIZATION_HEADER = "Authorization";

  @Test
  public void testAPlusAuthentication() {
    char[] token = new char[] {'a', 'b', 'c'};
    APlusAuthentication authentication = new APlusAuthentication(token);

    HttpRequest request = new HttpGet("https://example.com");
    authentication.addToRequest(request);

    Assert.assertEquals("The token should be added to the given request",
        "Token abc", request.getFirstHeader(AUTHORIZATION_HEADER).getValue());
  }

  @Test
  public void testMakesCopyOfArray() {
    char[] token = new char[] {'d', 'e', 'f'};
    APlusAuthentication authentication = new APlusAuthentication(token);

    token[0] = 'g';
    HttpRequest request = new HttpGet("https://example.org");
    authentication.addToRequest(request);

    Assert.assertEquals("The constructor makes a copy of the given array",
        "Token def", request.getFirstHeader(AUTHORIZATION_HEADER).getValue());
  }

  @Test
  public void testClear() {
    char[] token = new char[] {'g', 'h', 'i'};
    APlusAuthentication authentication = new APlusAuthentication(token);

    HttpRequest request = new HttpGet("http://localhost:1234");
    authentication.clear();
    authentication.addToRequest(request);

    Assert.assertEquals("The token is cleared", "Token \0\0\0",
        request.getFirstHeader(AUTHORIZATION_HEADER).getValue());
  }

}
