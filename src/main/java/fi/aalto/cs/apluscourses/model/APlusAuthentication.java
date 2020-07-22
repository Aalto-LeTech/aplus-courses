package fi.aalto.cs.apluscourses.model;

import java.util.Arrays;
import org.apache.http.HttpRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class APlusAuthentication implements Authentication {
  @NotNull
  private final char[] token;

  /**
   * Initializes an instance with the given token. Note, that the given token array is not cleared
   * or overwritten.
   */
  public APlusAuthentication(@NotNull char[] token) {
    this.token = token.clone();
  }

  @Override
  public void addToRequest(@NotNull HttpRequest request) {
    synchronized (token) {
      request.addHeader("Authorization", "Token " + new String(token));
    }
  }

  @Override
  public void clear() {
    synchronized (token) {
      Arrays.fill(token, '\0');
    }
  }

  /**
   * Returns true if the token represents the same string as the parameter.
   *
   * @param string String to compare.
   * @return True, if strings are equal, otherwise false.
   */
  public synchronized boolean tokenEquals(@Nullable String string) {
    synchronized (token) {
      return new String(token).equals(string);
    }
  }
}
