package fi.aalto.cs.apluscourses.model;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class User {

  private static final Logger logger = LoggerFactory.getLogger(User.class);

  @NotNull
  private final Authentication authentication;

  @NotNull
  private String userName = "";

  /**
   * Constructor for User that fetches the user's name given an Authentication.
   */
  public User(@NotNull Authentication authentication,
              @NotNull ExerciseDataSource exerciseDataSource) {
    this.authentication = authentication;
    try {
      this.userName = exerciseDataSource.getUserName(authentication);
    } catch (IOException e) {
      logger.error("Failed to fetch user data", e);
    }
  }

  @NotNull
  public Authentication getAuthentication() {
    return authentication;
  }

  @NotNull
  public String getUserName() {
    return userName;
  }
}
