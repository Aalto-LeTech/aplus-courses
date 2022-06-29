package fi.aalto.cs.apluscourses.model;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public class Group {

  public static class GroupMember {
    private final long id;
    private final @NotNull String name;

    public long getId() {
      return id;
    }

    @NotNull
    public String getName() {
      return name;
    }

    public GroupMember(long id, @NotNull String name) {
      this.id = id;
      this.name = name;
    }
  }

  public static final @NotNull Group GROUP_ALONE = new Group(-1, Collections
      .singletonList(new GroupMember(-1, getText("ui.toolWindow.subTab.exercises.submission.submitAlone"))));

  private final long id;

  @NotNull
  private final List<GroupMember> members;

  public Group(long id, @NotNull List<GroupMember> members) {
    this.id = id;
    this.members = members;
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
    List<GroupMember> memberNames = new ArrayList<>(members.length());
    for (int i = 0; i < members.length(); ++i) {
      GroupMember member = new GroupMember(members.getJSONObject(i).getInt("id"),
          members.getJSONObject(i).getString("full_name"));
      memberNames.add(member);
    }
    return new Group(id, memberNames);
  }

  public long getId() {
    return id;
  }

  @NotNull
  public List<String> getMemberNames() {
    return members.stream().map(x -> x.name).collect(Collectors.toUnmodifiableList());
  }

  /**
   * Returns an identifier for the group, based on the members of the group. The difference between this and
   * {@link #getId() getId} is that this method will return the same ID for two different groups if their
   * members are the same.
   */
  @NotNull
  public String getMemberwiseId() {
    return members.stream().map(x -> x.id).sorted().map(String::valueOf).collect(Collectors.joining(","));
  }

  @Override
  public int hashCode() {
    return Long.hashCode(id);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Group && ((Group) obj).getId() == getId();
  }

  @Override
  public String toString() {
    return "Group{" + getMemberNames() + '}';
  }
}
