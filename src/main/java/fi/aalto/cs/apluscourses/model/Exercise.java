package fi.aalto.cs.apluscourses.model;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class Exercise {

  private final long id;

  @NotNull
  private final String name;

  @NotNull
  private final String htmlUrl;

  @NotNull
  private final List<Long> submissionIds;

  private final int userPoints;

  private final int maxPoints;

  private final int maxSubmissions;

  /**
   * Construct an exercise instance with the given parameters.
   *
   * @param id             The ID of the exercise.
   * @param name           The name of the exercise.
   * @param submissionIds  A list of IDs corresponding to submissions to this exercise.
   * @param userPoints     The best points that the user has gotten from this exercise.
   * @param maxPoints      The maximum points available from this exercise.
   * @param maxSubmissions The maximum number of submissions allowed for this exercise.
   */
  public Exercise(long id,
                  @NotNull String name,
                  @NotNull String htmlUrl,
                  @NotNull List<Long> submissionIds,
                  int userPoints,
                  int maxPoints,
                  int maxSubmissions) {
    this.id = id;
    this.name = name;
    this.htmlUrl = htmlUrl;
    this.submissionIds = submissionIds;
    this.userPoints = userPoints;
    this.maxPoints = maxPoints;
    this.maxSubmissions = maxSubmissions;
  }

  /**
   * Construct an exercise from the given JSON object. The object must contain an integer value for
   * the key "id", a string value for the key "display_name", a string value for the key "html_url",
   * and integer values for the keys "max_points" and "max_submissions".
   *
   * @param jsonObject The JSON object from which the exercise is constructed.
   * @return An exercise instance.
   */
  @NotNull
  public static Exercise fromJsonObject(@NotNull JSONObject jsonObject,
                                        @NotNull Points points) {
    long id = jsonObject.getLong("id");
    String name = jsonObject.getString("display_name");
    String htmlUrl = jsonObject.getString("html_url");
    List<Long> submissionIds = points.getSubmissions().getOrDefault(id, Collections.emptyList());
    int userPoints = points.getPoints().getOrDefault(id, 0);
    int maxPoints = jsonObject.getInt("max_points");
    int maxSubmissions = jsonObject.getInt("max_submissions");
    return new Exercise(id, name, htmlUrl, submissionIds, userPoints, maxPoints, maxSubmissions);
  }

  public long getId() {
    return id;
  }

  @NotNull
  public String getName() {
    return name;
  }

  @NotNull
  public String getHtmlUrl() {
    return htmlUrl;
  }

  @NotNull
  public List<Long> getSubmissionIds() {
    return Collections.unmodifiableList(submissionIds);
  }

  public int getUserPoints() {
    return userPoints;
  }

  public int getMaxPoints() {
    return maxPoints;
  }

  public int getMaxSubmissions() {
    return maxSubmissions;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(id);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Exercise && ((Exercise) obj).getId() == getId();
  }
}
