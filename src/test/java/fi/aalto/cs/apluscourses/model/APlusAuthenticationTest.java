package fi.aalto.cs.apluscourses.model;

import static fi.aalto.cs.apluscourses.model.APlusAuthentication.A_API;
import static fi.aalto.cs.apluscourses.model.APlusAuthentication.A_COURSES_PLUGIN;
import static org.junit.Assert.assertArrayEquals;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class APlusAuthenticationTest extends BasePlatformTestCase {

  private static final String AUTHORIZATION_HEADER = "Authorization";

  @Test
  public void testAPlusAuthentication() {
    char[] token = new char[]{'a', 'b', 'c'};
    APlusAuthentication authentication = new APlusAuthentication(token);

    HttpRequest request = new HttpGet("https://example.com");
    authentication.addToRequest(request);

    assertEquals("The token should be added to the given request",
        "Token abc", request.getFirstHeader(AUTHORIZATION_HEADER).getValue());
  }

  @Test
  public void testMakesCopyOfArray() {
    char[] token = new char[]{'d', 'e', 'f'};
    APlusAuthentication authentication = new APlusAuthentication(token);

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
    char[] token = new char[]{'g', 'h', 'i'};
    APlusAuthentication authentication = new APlusAuthentication(token);

    CredentialAttributes credentialAttributes = Whitebox
        .invokeMethod(authentication, "createCredentialAttributes");
    String serviceName = credentialAttributes.getServiceName();

    assertEquals("The credentials attributes have been created with a correct service name.",
        "IntelliJ Platform " + A_COURSES_PLUGIN + " — " + A_API, serviceName);
  }

  @Test
  public void testGetToken() {
    char[] token = new char[]{'g', 'h', 'i'};
    APlusAuthentication authentication = new APlusAuthentication(token);

    char[] extractedToken = authentication.getToken();

    assertArrayEquals("The extracted token is as expected.", token, extractedToken);
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
