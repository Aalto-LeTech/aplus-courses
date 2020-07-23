package fi.aalto.cs.apluscourses.model;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import java.util.Arrays;
import org.apache.http.HttpRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class APlusAuthentication implements Authentication {

  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String A_COURSES_PLUGIN = "A+ Courses Plugin";
  public static final String A_API = "A+ API";

  private final Object authenticationLock = new Object();

  @NotNull
  private final CredentialAttributes credentialAttributes;

  public APlusAuthentication() {
    this.credentialAttributes = createCredentialAttributes();
  }

  /**
   * Initializes an instance with the given token. Note, that the given token array is not cleared
   * or overwritten.
   */
  public APlusAuthentication(@NotNull char[] token) {
    this();
    setToken(token.clone());
  }

  public void setToken(@NotNull char[] token) {
    Credentials credentials = new Credentials("A+_user", token.clone());
    PasswordSafe.getInstance().set(credentialAttributes, credentials);
  }

  /**
   * The getter for a {@link PasswordSafe}-stored token.
   *
   * @return a token as char[] OR 'null' if empty.
   */
  @Nullable
  public char[] getToken() {
    Credentials credentials = PasswordSafe.getInstance().get(credentialAttributes);
    if (credentials != null) {
      return credentials.getPassword().toCharArray(true);
    }
    return null;
  }

  public void deleteTokenFromStorage() {
    PasswordSafe.getInstance().set(credentialAttributes, null);
  }

  @Override
  public void addToRequest(@NotNull HttpRequest request) {
    synchronized (authenticationLock) {
      char[] token = getToken();
      if (token != null) {
        request.addHeader(AUTHORIZATION_HEADER, "Token " + new String(token));
      }
    }
  }

  /**
   * Returns true if the token represents the same string as the parameter.
   *
   * @param string String to compare.
   * @return True, if strings are equal, otherwise false.
   */
  public synchronized boolean tokenEquals(@Nullable String string) {
    synchronized (authenticationLock) {
      char[] token = getToken();
      if (token != null) {
        return new String(token).equals(string);
      }
    }
    return false;
  }

  @NotNull
  private CredentialAttributes createCredentialAttributes() {
    return new CredentialAttributes(
        CredentialAttributesKt.generateServiceName(A_COURSES_PLUGIN, A_API));
  }
}
