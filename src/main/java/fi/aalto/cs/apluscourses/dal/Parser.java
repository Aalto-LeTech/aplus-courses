package fi.aalto.cs.apluscourses.dal;

import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.SubmissionHistory;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.json.JSONArray;
import org.json.JSONObject;

public interface Parser {

  SubmissionInfo parseSubmissionInfo(JSONObject object);

  SubmissionHistory parseSubmissionHistory(JSONObject object);

  Group parseGroup(JSONObject object);

  ExerciseGroup parseExerciseGroup(JSONObject object);

  /**
   * Parses an JSON array to a list using a given parsing function.
   *
   * @param array JSON array.
   * @param parse Function that parses an JSONObject to T object.
   * @param <T>   Type of the result items.
   * @return A list whose items are parsed from JSON objects of the given array.
   */
  default <T> List<T> parseArray(JSONArray array, Function<JSONObject, T> parse) {
    int length = array.length();
    List<T> results = new ArrayList<>(length);
    for (int i = 0; i < length; i++) {
      results.add(parse.apply(array.getJSONObject(i)));
    }
    return results;
  }
}
