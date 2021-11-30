package fi.aalto.cs.apluscourses.utils.cache;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class JsonFileCache extends FileCache<String, JSONObject> {

  public JsonFileCache(@NotNull Path filePath) {
    super(filePath);
  }

  @Override
  protected @NotNull Map<String, CacheEntry<JSONObject>> fromFile(@NotNull File file) throws IOException {
    Map<String, CacheEntry<JSONObject>> entries = new HashMap<>();
    var json = new JSONObject(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
    Iterable<String> keys = json::keys;
    for (String url : keys) {
      var entryJson = json.getJSONObject(url);
      var createdAt = ZonedDateTime.parse(entryJson.getString("createdAt"));
      var value = entryJson.getJSONObject("value");
      entries.put(url, new CacheEntry<>(value, createdAt));
    }
    return entries;
  }

  @Override
  protected void toFile(@NotNull File file,
                        @NotNull Map<String, CacheEntry<JSONObject>> entries) throws IOException {
    var json = new JSONObject();
    entries.forEach((url, entry) -> {
      var entryJson = new JSONObject();
      entryJson.put("createdAt", entry.getCreationTime().toString());
      entryJson.put("value", entry.getValue());
      json.put(url, entryJson);
    });
    FileUtils.writeStringToFile(file, json.toString(), StandardCharsets.UTF_8);
  }

}
