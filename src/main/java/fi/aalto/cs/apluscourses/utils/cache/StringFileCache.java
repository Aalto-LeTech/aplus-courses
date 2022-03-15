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

public class StringFileCache extends FileCache<String, String> {

  public StringFileCache(@NotNull Path filePath) {
    super(filePath);
  }

  @Override
  protected @NotNull Map<String, CacheEntry<String>> fromFile(@NotNull File file) throws IOException {
    Map<String, CacheEntry<String>> entries = new HashMap<>();
    var json = new JSONObject(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
    Iterable<String> keys = json::keys;
    for (String key : keys) {
      var value = json.getString(key);
      entries.put(key, new CacheEntry<>(value, ZonedDateTime.now()));
    }
    return entries;
  }

  @Override
  protected void toFile(@NotNull File file, @NotNull Map<String, CacheEntry<String>> entries) throws IOException {
    var json = new JSONObject();
    entries.forEach((key, entry) -> json.put(key, entry.getValue()));
    FileUtils.writeStringToFile(file, json.toString(), StandardCharsets.UTF_8);
  }
}
