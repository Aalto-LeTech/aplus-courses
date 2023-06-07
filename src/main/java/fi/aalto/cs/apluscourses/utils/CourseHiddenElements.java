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

  /**
   * Constructs the CourseHiddenElements object from an array of JSON objects.
   * Each JSON object must either have a "byId" integer-valued element (in order to
   * hide the element by its ID), a "byRegex" string-valued element (to hide the element
   * by a case-insensitive regex match), or a "byRegex" object-valued element (same as string,
   * but language-specific).
   */
  @NotNull
  public static CourseHiddenElements fromJsonObject(@NotNull JSONArray hiddenElementsArray) {
    List<Long> hiddenIDs = new ArrayList<>();
    List<Pattern> hiddenRegexes = new ArrayList<>();
    Map<String, List<Pattern>> hiddenLanguageSpecificRegexes = new HashMap<>();

    for (int i = 0; i < hiddenElementsArray.length(); ++i) {
      Object hiddenObject = hiddenElementsArray.get(i);

      if (hiddenObject instanceof Integer hiddenID) {
        hiddenIDs.add((long) hiddenID); // hiding by object ID
      } else if (hiddenObject instanceof String hiddenRegex) {
        hiddenRegexes.add(Pattern.compile(hiddenRegex, Pattern.CASE_INSENSITIVE)); // hiding by regex
      } else if (hiddenObject instanceof JSONObject hiddenRegexObject) {
        for (String key : hiddenRegexObject.keySet()) { // hiding by language-specific regex
          String regex = hiddenRegexObject.getString(key);
          hiddenLanguageSpecificRegexes.computeIfAbsent(key, k -> new ArrayList<>())
              .add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
        }
      } else {
        throw new JSONException("Element number " + i + " in the \"hiddenElements\" array is of an unsupported type");
      }
    }

    return new CourseHiddenElements(hiddenIDs, hiddenRegexes, hiddenLanguageSpecificRegexes);
  }

  /**
   * Checks if the object (e.g. exercise) ID or string match at least one of hiding rules.
   *
   * @return True of the object is supposed to be hidden, false otherwise.
   */
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

  /**
   * Constructor without any hiding rules.
   */
  public CourseHiddenElements() {
    this(null, null, null);
  }

  /**
   * Constructor.
   */
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
