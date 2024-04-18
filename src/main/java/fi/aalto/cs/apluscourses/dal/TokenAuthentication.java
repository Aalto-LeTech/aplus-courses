package fi.aalto.cs.apluscourses.dal;

import fi.aalto.cs.apluscourses.model.Authentication;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TokenAuthentication implements Authentication {
  @NotNull
  protected final String user;
  protected final char @NotNull [] token;
  @Nullable
  protected final TokenStorage passwordStorage;

  /**
   * Base class constructor.
   *
   * @param user            Name of the user that is referred to when persisting the token
   * @param token           Token.  The array is cloned and the argument is not cleared.
   * @param passwordStorage Password storage.  If null, the authentication won't be persisted.
   */
  protected TokenAuthentication(@NotNull String user,
                                char @NotNull [] token,
                                @Nullable TokenStorage passwordStorage) {
    this.user = user;
    this.token = token.clone();
    this.passwordStorage = passwordStorage;
  }

  @Override
  public boolean persist() {
    return false;//passwordStorage != null && passwordStorage.store(user, token);
  }

  /**
   * Returns true if the token represents the same string as the parameter.
   *
   * @param string String to compare.
   * @return True, if strings are equal, otherwise false.
   */
  public boolean tokenEquals(@Nullable String string) {
    return new String(token).equals(string);
  }

  @Override
  public void clear() {
    Arrays.fill(token, '\0');
  }

  public interface Factory {
    @NotNull
    TokenAuthentication create(char[] token);
  }
}
