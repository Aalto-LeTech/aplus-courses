package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.dal.TokenAuthentication;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import java.io.IOException;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AuthenticationViewModel {
  @NotNull
  private final TokenAuthentication.Factory authenticationFactory;

  @NotNull
  private final String authenticationUrl;

  @NotNull
  private final ExerciseDataSource exerciseDataSource;

  @Nullable
  private Authentication authentication;

  private char @Nullable [] token;
  private final Object lock = new Object();

  /**
   * Constructs an AuthenticationViewModel.
   */
  public AuthenticationViewModel(@NotNull TokenAuthentication.Factory authenticationFactory,
                                 @NotNull String authenticationHtmlUrl,
                                 @NotNull ExerciseDataSource exerciseDataSource) {
    this.authenticationFactory = authenticationFactory;
    this.authenticationUrl = authenticationHtmlUrl;
    this.exerciseDataSource = exerciseDataSource;
  }

  /**
   * Sets token by cloning the given token.
   *
   * @param tokenToBeCloned A token.
   */
  public void setToken(char @NotNull [] tokenToBeCloned) {
    synchronized (lock) {
      token = tokenToBeCloned.clone();
    }
  }

  /**
   * Builds an authentication object based on the data given to this view model and clears that
   * data from memory.
   */
  public void build() {
    synchronized (lock) {
      if (token == null) {
        throw new IllegalStateException("Token is not set");
      }
      this.authentication = authenticationFactory.create(token);
      Arrays.fill(token, '\0');
    }
  }

  @NotNull
  public String getAuthenticationHtmlUrl() {
    return authenticationUrl;
  }

  public void tryGetUser(Authentication authentication) throws IOException {
    exerciseDataSource.getUser(authentication);
  }

  @Nullable
  public Authentication getAuthentication() {
    return authentication;
  }
}
