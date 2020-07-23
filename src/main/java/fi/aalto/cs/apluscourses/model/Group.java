package fi.aalto.cs.apluscourses.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public class Group {

  private final long id;

  private final List<String> memberNames;

  public Group(long id, @NotNull List<String> memberNames) {
    this.id = id;
    this.memberNames = memberNames;
  }

  /**
   * Construct a group from the given JSON object. The JSON object must contain an integer ID for
   * the key "id", as well as an array for the key "members", where each value of the array is a
   * JSON object containing a string for the key "full_name".
   */
  @NotNull
  public static Group fromJsonObject(@NotNull JSONObject jsonObject) {
    long id = jsonObject.getLong("id");
    JSONArray members = jsonObject.getJSONArray("members");
    List<String> memberNames = new ArrayList<>(members.length());
    for (int i = 0; i < members.length(); ++i) {
      memberNames.add(members.getJSONObject(i).getString("full_name"));
    }
    return new Group(id, memberNames);
  }

  public long getId() {
    return id;
  }

  @NotNull
  public List<String> getMemberNames() {
    return Collections.unmodifiableList(memberNames);
  }


  @Override
  public int hashCode() {
    return Long.hashCode(id);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Group && ((Group) obj).getId() == getId();
  }
}
