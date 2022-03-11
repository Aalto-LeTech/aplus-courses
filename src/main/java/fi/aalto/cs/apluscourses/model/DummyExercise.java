package fi.aalto.cs.apluscourses.model;

import java.util.OptionalLong;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class DummyExercise extends Exercise {

  /**
   * A constructor.
   */
  public DummyExercise(long id,
                       @NotNull String name,
                       @NotNull String htmlUrl,
                       @NotNull SubmissionInfo submissionInfo,
                       int maxPoints,
                       int maxSubmissions,
                       @Nullable String difficulty,
                       boolean isOptional) {
    super(id, name, htmlUrl, submissionInfo, maxPoints, maxSubmissions,
        OptionalLong.empty(), difficulty, isOptional);
  }

  /**
   * Constructs a DummyExercise from a JsonObject.
   */
  @NotNull
  public static DummyExercise fromJsonObject(@NotNull JSONObject jsonObject) {
    long id = jsonObject.getLong("id");

    String name = jsonObject.getString("display_name");
    String htmlUrl = jsonObject.getString("html_url");

    int maxPoints = jsonObject.getInt("max_points");
    int maxSubmissions = jsonObject.getInt("max_submissions");

    String difficulty = jsonObject.optString("difficulty");

    var submissionInfo = SubmissionInfo.fromJsonObject(jsonObject);

    return new DummyExercise(id, name, htmlUrl, submissionInfo, maxPoints, maxSubmissions, difficulty, false);
  }

  @Override
  public boolean isDummy() {
    return true;
  }
}
