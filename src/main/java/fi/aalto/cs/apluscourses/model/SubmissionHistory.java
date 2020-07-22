package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class SubmissionHistory {

  private final int numberOfSubmissions;

  public SubmissionHistory(int numberOfSubmissions) {
    this.numberOfSubmissions = numberOfSubmissions;
  }

  @NotNull
  public static SubmissionHistory fromJsonObject(@NotNull JSONObject jsonObject) {
    int count = jsonObject.getInt("count");
    return new SubmissionHistory(count);
  }

  public int getNumberOfSubmissions() {
    return numberOfSubmissions;
  }

}
