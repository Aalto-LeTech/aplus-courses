package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.Authentication;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AuthenticationViewModel {
  @Nullable
  private char[] token;
  private final Object lock = new Object();

  /**
   * Sets token by cloning the given token.
   *
   * @param tokenToBeCloned A token.
   */
  public void setToken(@NotNull char[] tokenToBeCloned) {
    synchronized (lock) {
      if (token != null) {
        throw new IllegalStateException("Token can be set only once");
      }
      token = tokenToBeCloned.clone();
    }
  }

  /**
   * Builds an authentication object based on the data given to this view model and clears that
   * data from memory.
   *
   * @return A new {@link Authentication} object.
   */
  @NotNull
  public Authentication build() {
    synchronized (lock) {
      if (token == null) {
        throw new IllegalStateException("Token is not set");
      }
      Authentication authentication = new APlusAuthentication(token);
      Arrays.fill(token, '\0');
      return authentication;
    }
  }
}
