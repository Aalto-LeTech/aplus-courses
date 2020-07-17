package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.CoursesClient;
import java.util.Arrays;
import org.apache.http.HttpRequest;
import org.jetbrains.annotations.NotNull;

public class APlusAuthentication implements Authentication {
  @NotNull
  private final char[] token;
  private final int maxLength;
  private int length = 0;

  public APlusAuthentication(int maxLength) {
    this.token = new char[maxLength];
    this.maxLength = maxLength;
  }

  public synchronized void addToRequest(@NotNull HttpRequest request) {
    if (!isSet()) {
      throw new IllegalStateException("Token is not set");
    }
    request.addHeader("Authorization", "Token " + new String(token, 0, length));
  }

  @Override
  public synchronized void clear() {
    setToken(new char[0]);
  }

  @Override
  public int maxTokenLength() {
    return maxLength;
  }

  /**
   * Sets the token by copying the given array.
   * Note, that the given array is not cleared or overwritten.
   *
   * @param newToken A token to be set.
   *                 In case of an empty array, this method works like {@code clear()}.
   */
  @Override
  public synchronized void setToken(@NotNull char[] newToken) {
    if (newToken.length > maxLength) {
      throw new IllegalArgumentException("Token is too long");
    }
    length = newToken.length;
    System.arraycopy(newToken, 0, token, 0, length);
    Arrays.fill(token, length, maxLength, '\0');
  }

  @Override
  public synchronized boolean isSet() {
    return length > 0;
  }
}
