package fi.aalto.cs.apluscourses.utils;

import fi.aalto.cs.apluscourses.model.MalformedCourseConfigurationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CourseHiddenElements {

  private final @NotNull List<Integer> hiddenIDs = new ArrayList<>();

  private final @NotNull List<String> hiddenRegexes = new ArrayList<>();

  private final @NotNull Map<String, List<String>> hiddenLanguageSpecificRegexes = new HashMap<>();

  @NotNull
  public static CourseHiddenElements fromJsonObject(@NotNull JSONArray hiddenEntriesArray) {
    List<Integer> hiddenIDs = new ArrayList<>();
    List<String> hiddenRegexes = new ArrayList<>();
    Map<String, List<String>> hiddenLanguageSpecificRegexes = new HashMap<>();

    for (int i = 0; i < hiddenEntriesArray.length(); ++i) {
      JSONObject entryInfo = hiddenEntriesArray.getJSONObject(i);

      int hiddenID = entryInfo.optInt("byId", -1);
      if (hiddenID != -1) {
        // Hide by exercise/group ID
        hiddenIDs.add(hiddenID);
        continue;
      }

      Object hiddenRegex = entryInfo.get("byRegex");
      if (hiddenRegex instanceof String) {
        // Hide by language-independent regex
        hiddenRegexes.add((String) hiddenRegex);
        continue;
      } else if (hiddenRegex instanceof JSONObject) {
        // Hide by language-dependent regex
        JSONObject hiddenRegexObject = (JSONObject)hiddenRegex;
        for (String key : hiddenRegexObject.keySet()) {
          String regex = hiddenRegexObject.getString(key);
          hiddenLanguageSpecificRegexes.computeIfAbsent(key, k -> new ArrayList<>()).add(regex);
        }

        continue;
      }

      throw new JSONException("Element number " + i + " in the \"hiddenEntries\" array is of an unsupported format");
    }

    return new CourseHiddenElements(hiddenIDs, hiddenRegexes, hiddenLanguageSpecificRegexes);
  }

  public CourseHiddenElements() {
    this(null, null, null);
  }

  public CourseHiddenElements(@Nullable List<Integer> hiddenIDs,
                              @Nullable List<String> hiddenRegexes,
                              @Nullable Map<String, List<String>> hiddenLanguageSpecificRegexes) {
    if (hiddenIDs != null) {
      this.hiddenIDs.addAll(hiddenIDs);
    }

    if (hiddenRegexes != null) {
      this.hiddenRegexes.addAll(hiddenRegexes);
    }

    if (hiddenLanguageSpecificRegexes != null) {
      this.hiddenLanguageSpecificRegexes.putAll(hiddenLanguageSpecificRegexes);
    }
  }
}
