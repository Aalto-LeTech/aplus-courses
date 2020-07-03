package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.CoursesClient;
import java.util.Arrays;
import org.apache.http.HttpRequest;
import org.jetbrains.annotations.NotNull;

public class APlusAuthentication implements CoursesClient.Authentication {
  @NotNull
  private final char[] token;

  /**
   * Initializes an instance with the given token. Note, that the given token array is not cleared
   * or overwritten.
   */
  public APlusAuthentication(@NotNull char[] token) {
    this.token = token.clone();
  }

  public void addToRequest(@NotNull HttpRequest request) {
    request.addHeader("Authorization", "Token " + new String(token));
  }

  public void clear() {
    Arrays.fill(token, '\0');
  }
}
