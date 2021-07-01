package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class Student {
  private final long id;
  @NotNull
  private final String fullName;
  @NotNull
  private final String studentId;
  @NotNull
  private final String pointsUrl;

  public Student(long id,
                 @NotNull String fullName,
                 @NotNull String studentId,
                 @NotNull String pointsUrl) {

    this.id = id;
    this.fullName = fullName;
    this.studentId = studentId;
    this.pointsUrl = pointsUrl;
  }

  public static Student fromJsonObject(@NotNull JSONObject jsonObject) {
    var sId = jsonObject.getLong("id");
    var sFullName = jsonObject.getString("full_name");
    var sStudentId = jsonObject.getString("student_id");
    var sPointsUrl = jsonObject.getString("points");
    return new Student(sId, sFullName, sStudentId, sPointsUrl);
  }

  @NotNull
  public String getFullName() {
    return fullName;
  }

  public long getId() {
    return id;
  }
}
