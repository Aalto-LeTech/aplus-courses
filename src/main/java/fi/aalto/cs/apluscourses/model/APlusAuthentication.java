package fi.aalto.cs.apluscourses.model;

import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public class APlusAuthentication {
  @NotNull
  private final char[] token;

  /**
   * Initializes an instance with the given token. Note, that the given token array is not cleared
   * or overwritten.
   */
  public APlusAuthentication(@NotNull char[] token) {
    this.token = token.clone();
  }

  @NotNull
  public char[] getToken() {
    return token;
  }

  public void clear() {
    Arrays.fill(token, '\0');
  }
}
