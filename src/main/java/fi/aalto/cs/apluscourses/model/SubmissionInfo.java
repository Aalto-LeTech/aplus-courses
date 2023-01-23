package fi.aalto.cs.apluscourses.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public class SubmissionInfo {
  @NotNull
  private final Map<String, List<SubmittableFile>> files;

  /**
   * Construct a submission info instance with the given files.
   *
   * @param files A map from language codes to list of submittable files corresponding to the
   *              language.
   */
  public SubmissionInfo(@NotNull Map<String, List<SubmittableFile>> files) {
    this.files = files;
  }

  /**
   * Construct a submission info instance from the given JSON object.
   */
  @NotNull
  public static SubmissionInfo fromJsonObject(@NotNull JSONObject jsonObject) {
    var exerciseInfo = jsonObject.optJSONObject("exercise_info");
    if (exerciseInfo == null) {
      // Some assignments, such as https://plus.cs.aalto.fi/api/v2/exercises/24882/ don't have the
      // exercise info at all.
      return new SubmissionInfo(Collections.emptyMap());
    }

    JSONArray formSpec = exerciseInfo.getJSONArray("form_spec");
    JSONObject localizationInfo = exerciseInfo.getJSONObject("form_i18n");
    Map<String, List<SubmittableFile>> files = new HashMap<>();

    for (int i = 0; i < formSpec.length(); ++i) {
      JSONObject spec = formSpec.getJSONObject(i);
      String type = spec.optString("type");
      if (!"file".equals(type)) {
        continue;
      }

      String key = spec.getString("key");
      String title = spec.getString("title");
      JSONObject localizedFilenames = localizationInfo.getJSONObject(title);

      Iterable<String> languages = localizedFilenames::keys;
      for (String language : languages) {
        List<SubmittableFile> filesForLanguage = files.getOrDefault(language, new ArrayList<>());
        filesForLanguage.add(new SubmittableFile(key, localizedFilenames.getString(language)));
        files.putIfAbsent(language, filesForLanguage);
      }
    }
    return new SubmissionInfo(files);
  }

  /**
   * Returns the submittable files corresponding to the given language (or an empty collection if
   * the language isn't found).
   */
  @NotNull
  public List<SubmittableFile> getFiles(@NotNull String language) {
    return files.getOrDefault(language, Collections.emptyList());
  }

  /**
   * Returns true if there is some language in which the exercise can be submitted from the IDE.
   */
  public boolean isSubmittable() {
    return !files.isEmpty();
  }

  /**
   * Returns true if the exercise can be submitted in the given language from the IDE.
   */
  public boolean isSubmittable(@NotNull String language) {
    return !getFiles(language).isEmpty();
  }
}
