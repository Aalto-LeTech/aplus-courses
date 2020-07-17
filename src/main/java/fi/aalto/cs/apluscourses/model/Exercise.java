package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.LongIdObject;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class Exercise extends LongIdObject {

  @NotNull
  private String name;

  public Exercise(long id, @NotNull String name) {
    super(id);
    this.name = name;
  }

  /**
   * Construct an exercise from the given JSON object. The object must contain an integer value for
   * the key "id" and a string value for the key "display_name".
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
  public String getName() {
    return name;
  }
}
