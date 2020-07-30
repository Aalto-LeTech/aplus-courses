package fi.aalto.cs.apluscourses.dal;

import org.apache.http.HttpRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class APlusTokenAuthentication extends TokenAuthentication {

  public static final String AUTHORIZATION_HEADER = "Authorization";

  public static final String APLUS_USER = "A+_user";

  /**
   * Initializes an instance with the given token. Note, that the given token array is not cleared
   * or overwritten.
   */
  public APlusTokenAuthentication(@NotNull char[] token,
                                  @Nullable PasswordStorage passwordStorage) {
    super(APLUS_USER, token, passwordStorage);
  }

  /**
   * Creates an authentication object that cannot be persisted.
   *
   * @param token Token.
   */
  public APlusTokenAuthentication(@NotNull char[] token) {
    this(token, null);
  }

  @Override
  public void addToRequest(@NotNull HttpRequest request) {
    request.addHeader(AUTHORIZATION_HEADER, "Token " + new String(token));
  }

  public static Factory getFactoryFor(@Nullable PasswordStorage passwordStorage) {
    return token -> new APlusTokenAuthentication(token, passwordStorage);
  }
}
