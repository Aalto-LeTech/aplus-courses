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

  private final List<GroupMember> memberNames;

  public Group(long id, @NotNull List<GroupMember> memberNames) {
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
    return memberNames.stream().map(x -> x.name).collect(Collectors.toUnmodifiableList());
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
