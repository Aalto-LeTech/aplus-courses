package fi.aalto.cs.apluscourses.model;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import org.apache.http.HttpRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class APlusAuthentication implements CoursesClient.Authentication {

  public static final String A_COURSES_PLUGIN = "A+ Courses Plugin";
  public static final String A_API = "A+ API";

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

  public void addToRequest(@NotNull HttpRequest request) {
    request.addHeader("Authorization", "Token " + new String(getToken()));
  }

  @NotNull
  private CredentialAttributes createCredentialAttributes() {
    return new CredentialAttributes(
        CredentialAttributesKt.generateServiceName(A_COURSES_PLUGIN, A_API));
  }
}
