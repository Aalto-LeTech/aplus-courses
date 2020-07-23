package fi.aalto.cs.apluscourses.model;

import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class APlusAuthenticationTest extends BasePlatformTestCase {

  private static final String AUTHORIZATION_HEADER = "Authorization";

  @Test
  public void testAPlusAuthentication() {
    Authentication authentication = new APlusAuthentication(new char[]{'a', 'b', 'c'});

    HttpRequest request = new HttpGet("https://example.com");
    authentication.addToRequest(request);

    assertEquals("The token should be added to the given request",
        "Token abc", request.getFirstHeader(AUTHORIZATION_HEADER).getValue());
  }

  @Test
  public void testMakesCopyOfArray() {
    char[] token = (new char[]{'d', 'e', 'f'});

    Authentication authentication = new APlusAuthentication(token);

    token[0] = 'g';
    HttpRequest request = new HttpGet("https://example.org");
    authentication.addToRequest(request);

    assertEquals("The constructor makes a copy of the given array",
        "Token def", request.getFirstHeader(AUTHORIZATION_HEADER).getValue());
  }

  @Test
  public void testClearToken() {
    char[] token = new char[]{'g', 'h', 'i'};
    APlusAuthentication authentication = new APlusAuthentication(token);

    authentication.deleteTokenFromStorage();

    assertNull("The token is cleared (set to 'null').", authentication.getToken());
  }

  @Test
  public void testCreateCredentialAttributes() throws Exception {
    APlusAuthentication authentication = new APlusAuthentication(new char[]{'g', 'h', 'i'});

    HttpRequest request = new HttpGet("http://localhost:1234");
    authentication.clear();
    authentication.addToRequest(request);

    Assert.assertEquals("The token is cleared", "Token \0\0\0",
        request.getFirstHeader(AUTHORIZATION_HEADER).getValue());
  }

  @Test
  public void testGetTokenReturnsNullForNull() {
    char[] token = new char[]{'g', 'h', 'i'};
    APlusAuthentication authentication = new APlusAuthentication(token);
    CredentialAttributes credentialAttributes = new CredentialAttributes(
        CredentialAttributesKt.generateServiceName(A_COURSES_PLUGIN, A_API));
    PasswordSafe.getInstance().set(credentialAttributes, null);

    char[] extractedToken = authentication.getToken();

    assertNull("The extracted token is 'null'.", extractedToken);
  }

  @Test
  public void testSetToken() {
    char[] token = new char[]{'g', 'h', 'i'};
    APlusAuthentication authentication = new APlusAuthentication(token);

    char[] newToken = new char[]{'a', 'b', 'c'};
    authentication.setToken(newToken);

    assertArrayEquals("The new token is as expected.", newToken, authentication.getToken());
  }
}
