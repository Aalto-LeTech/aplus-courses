package fi.aalto.cs.apluscourses.dal;

import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.utils.cache.CachePreference;
import fi.aalto.cs.apluscourses.utils.cache.CachePreferences;
import java.io.IOException;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public interface Client {
  JSONObject fetch(@NotNull String url,
                   @NotNull Authentication authentication,
                   @NotNull CachePreference cachePreference) throws IOException;

  default JSONObject fetch(@NotNull String url,
                           @NotNull Authentication authentication) throws IOException {
    // Ignore all cache entries by default
    return fetch(url, authentication, CachePreferences.GET_NEW_AND_FORGET);
  }

  String post(@NotNull String url,
              @NotNull Authentication authentication,
              @NotNull Map<String, Object> data) throws IOException;

  default void clearCache() {}

  default void updateCacheExpiration(long courseLastModified) {}
}
