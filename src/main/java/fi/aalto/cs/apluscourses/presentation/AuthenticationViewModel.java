package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.Authentication;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AuthenticationViewModel {
  @Nullable
  private volatile char[] token;

  public void setToken(@NotNull char[] token) {
    this.token = token.clone();
  }

  /**
   * Builds an authentication object based on the data given to this view model and clears that
   * data from memory.
   *
   * @return A new {@link Authentication} object.
   */
  @NotNull
  public Authentication build() {
    char[] localToken = token;
    if (localToken == null) {
      throw new IllegalStateException("Token is not set");
    }
    Authentication authentication = new APlusAuthentication(localToken);
    Arrays.fill(localToken, '\0');
    return authentication;
  }
}
