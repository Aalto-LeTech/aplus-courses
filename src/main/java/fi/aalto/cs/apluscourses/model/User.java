package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;

public class User {
  @NotNull
  private final Authentication authentication;

  @NotNull
  private final String userName;

  @NotNull
  private final String studentId;

  private final int id;

  /**
   * A constructor.
   */
  public User(@NotNull Authentication authentication,
              @NotNull String userName,
              @NotNull String studentId,
              int id) {
    this.authentication = authentication;
    this.userName = userName;
    this.studentId = studentId;
    this.id = id;
  }

  @NotNull
  public Authentication getAuthentication() {
    return authentication;
  }

  @NotNull
  public String getUserName() {
    return userName;
  }

  @NotNull
  public String getStudentId() {
    return studentId;
  }

  public int getId() {
    return id;
  }
}
