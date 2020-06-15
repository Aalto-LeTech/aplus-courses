package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class Exercise {
  @NotNull
  private String id;

  @NotNull
  private String name;

  public Exercise(long id, @NotNull String name) {
    this.id = String.valueOf(id);
    this.name = name;
  }

  /**
   * Construct an exercise from the given JSON object. The object must contain string values for the
   * keys "id" and "display_name".
   *
   * @param jsonObject The JSON object from which the exercise is constructed.
   * @return An exercise instance.
   */
  @NotNull
  public static Exercise fromJsonObject(@NotNull JSONObject jsonObject) {
    long id = jsonObject.getLong("id");
    String name = jsonObject.getString("display_name");
    return new Exercise(id, name);
  }

  @NotNull
  public String getId() {
    return id;
  }

  @NotNull
  public String getName() {
    return name;
  }
}
