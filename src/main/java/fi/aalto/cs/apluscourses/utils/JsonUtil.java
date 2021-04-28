package fi.aalto.cs.apluscourses.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonUtil {
  private JsonUtil() {

  }

  /**
   * Parses an JSON array to an array using a given parsing function.
   *
   * @param array JSON array.
   * @param getter E.g. JSONArray::getJSONObject, JSONArray::getString, ...
   * @param parser Function that parses JSON to T object.
   * @param ctor T[]::new.
   * @param <T> Type of the result items.
   * @param <J> Type of the input elements, e.g. JSONObject.
   * @return An array whose items are parsed from the elements of the given array.
   */
  public static <T, J> T[] parseArray(@NotNull JSONArray array,
                                      @NotNull BiFunction<JSONArray, Integer, J> getter,
                                      @NotNull Function<J, T> parser,
                                      @NotNull IntFunction<T[]> ctor) {
    int length = array.length();
    T[] result = ctor.apply(length);
    if (result.length != length) {
      throw new IllegalArgumentException("ctor must return an array of the given length.");
    }
    for (int i = 0; i < length; i++) {
      result[i] = getter.andThen(parser).apply(array, i);
    }
    return result;
  }

  /**
   *  Parses an JSON object to a map using a given parsing function.
   *
   * @param object JSON object.
   * @param getter E.g. JSONObject::getJSONObject, JSONObject::getString, ...
   * @param parser Function that parses an JSON to T object.
   * @param <T> Type of the result items.
   * @param <J> Type of the input elements, e.g. JSONObject.
   * @return A map whose entries are the properties of the given object.
   */
  public static <T, J, K> Map<K, T> parseObject(@NotNull JSONObject object,
                                                  @NotNull BiFunction<JSONObject, String, J> getter,
                                                  @NotNull Function<J, T> parser,
                                                  @NotNull Function<String, K> keyParser) {
    Map<K, T> result = new HashMap<>(object.length());
    for (String key : object.keySet()) {
      result.put(keyParser.apply(key), getter.andThen(parser).apply(object, key));
    }
    return result;
  }
}
