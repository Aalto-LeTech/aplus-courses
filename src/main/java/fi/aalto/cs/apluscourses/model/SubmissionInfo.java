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

  private final int submissionsLimit;

  @NotNull
  private final Map<String, List<SubmittableFile>> files;

  /**
   * Construct a submission info instance with the given submission limit and files.
   *
   * @param files A map from language codes to list of submittable files corresponding to the
   *              language.
   */
  public SubmissionInfo(int submissionsLimit, @NotNull Map<String, List<SubmittableFile>> files) {
    this.submissionsLimit = submissionsLimit;
    this.files = files;
  }

  /**
   * Construct a submission info instance from the given JSON object.
   */
  @NotNull
  public static SubmissionInfo fromJsonObject(@NotNull JSONObject jsonObject) {
    JSONObject exerciseInfo = jsonObject.getJSONObject("exercise_info");
    JSONArray formSpec = exerciseInfo.getJSONArray("form_spec");
    JSONObject localizationInfo = exerciseInfo.getJSONObject("form_i18n");
    Map<String, List<SubmittableFile>> files = new HashMap<>();

    for (int i = 0; i < formSpec.length(); ++i) {
      JSONObject spec = formSpec.getJSONObject(i);
      String type = spec.getString("type");
      if (!type.equals("file")) {
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

    int submissionLimit = jsonObject.getInt("max_submissions");

    return new SubmissionInfo(submissionLimit, files);
  }

  public int getSubmissionsLimit() {
    return submissionsLimit;
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
   * Returns true if the exercise can be submitted in the given language from the IDE.
   */
  public boolean isSubmittable(@NotNull String language) {
    return !getFiles(language).isEmpty();
  }
}
