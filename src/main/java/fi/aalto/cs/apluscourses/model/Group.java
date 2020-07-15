package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Group {

  private long id;

  private List<String> memberNames;

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

  /**
   * Get all of the groups from the A+ API for the user corresponding to the given authentication.
   * A group with id 0 and a single member name "Submit alone" is added to the beginning of the
   * list.
   *
   * @return A list of {@link Group}s that the user is a member of in the given course.
   * @throws IOException If an error occurs (e.g. network error).
   */
  @NotNull
  public static List<Group> getGroups(@NotNull Course course,
                                      @NotNull APlusAuthentication authentication)
      throws IOException {
    URL url =
        new URL(PluginSettings.A_PLUS_API_BASE_URL + "/courses/" + course.getId() + "/mygroups/");
    InputStream inputStream = CoursesClient.fetch(url, authentication::addToRequest);
    JSONObject response = new JSONObject(new JSONTokener(inputStream));
    JSONArray results = response.getJSONArray("results");
    List<Group> groups = new ArrayList<>(results.length() + 1);
    groups.add(new Group(0, Collections.singletonList("Submit alone")));
    for (int i = 0; i < results.length(); ++i) {
      groups.add(fromJsonObject(results.getJSONObject(i)));
    }
    return groups;
  }

  public long getId() {
    return id;
  }

  @NotNull
  public List<String> getMemberNames() {
    return Collections.unmodifiableList(memberNames);
  }
}
