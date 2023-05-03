package fi.aalto.cs.apluscourses.utils;

import fi.aalto.cs.apluscourses.model.MalformedCourseConfigurationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CourseHiddenElements {

  private final @NotNull List<Long> hiddenIDs = new ArrayList<>();

  private final @NotNull List<Pattern> hiddenRegexes = new ArrayList<>();

  private final @NotNull Map<String, List<Pattern>> hiddenLanguageSpecificRegexes = new HashMap<>();

  @NotNull
  public static CourseHiddenElements fromJsonObject(@NotNull JSONArray hiddenEntriesArray) {
    List<Long> hiddenIDs = new ArrayList<>();
    List<Pattern> hiddenRegexes = new ArrayList<>();
    Map<String, List<Pattern>> hiddenLanguageSpecificRegexes = new HashMap<>();

    for (int i = 0; i < hiddenEntriesArray.length(); ++i) {
      JSONObject entryInfo = hiddenEntriesArray.getJSONObject(i);

      long hiddenID = entryInfo.optLong("byId", -1);
      if (hiddenID != -1) {
        // Hide by exercise/group ID
        hiddenIDs.add(hiddenID);
        continue;
      }

      Object hiddenRegex = entryInfo.get("byRegex");
      if (hiddenRegex instanceof String) {
        // Hide by language-independent regex
        hiddenRegexes.add(Pattern.compile((String) hiddenRegex, Pattern.CASE_INSENSITIVE));
        continue;
      } else if (hiddenRegex instanceof JSONObject) {
        // Hide by language-dependent regex
        JSONObject hiddenRegexObject = (JSONObject)hiddenRegex;
        for (String key : hiddenRegexObject.keySet()) {
          String regex = hiddenRegexObject.getString(key);

          hiddenLanguageSpecificRegexes.computeIfAbsent(key, k -> new ArrayList<>())
              .add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
        }

        continue;
      }

      throw new JSONException("Element number " + i + " in the \"hiddenEntries\" array is of an unsupported format");
    }

    return new CourseHiddenElements(hiddenIDs, hiddenRegexes, hiddenLanguageSpecificRegexes);
  }

  public boolean shouldHideObject(long objectId, @NotNull String objectName, @Nullable String currentLanguage) {
    if (hiddenIDs.contains(objectId)) {
      return true;
    }

    if (hiddenRegexes.stream().anyMatch(r -> r.matcher(objectName).find())) {
      return true;
    }

    return currentLanguage != null && hiddenLanguageSpecificRegexes.getOrDefault(currentLanguage, new ArrayList<>())
        .stream().anyMatch(r -> r.matcher(objectName).find());
  }

  public CourseHiddenElements() {
    this(null, null, null);
  }

  public CourseHiddenElements(@Nullable List<Long> hiddenIDs,
                              @Nullable List<Pattern> hiddenRegexes,
                              @Nullable Map<String, List<Pattern>> hiddenLanguageSpecificRegexes) {
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
