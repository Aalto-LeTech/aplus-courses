package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;

public class APlusAuthentication {
  @NotNull
  private String token;

  public APlusAuthentication(@NotNull char[] token) {
    this.token = String.copyValueOf(token);
  }

  @NotNull
  public String getToken() {
    return token;
  }
}
