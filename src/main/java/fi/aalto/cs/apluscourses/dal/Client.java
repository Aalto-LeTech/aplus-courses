package fi.aalto.cs.apluscourses.dal;

import fi.aalto.cs.apluscourses.model.Authentication;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public interface Client {
  JSONObject fetch(@NotNull String url,
                   @NotNull Authentication authentication,
                   @NotNull ZonedDateTime minCacheEntryTime) throws IOException;

  default JSONObject fetch(@NotNull String url,
                           @NotNull Authentication authentication) throws IOException {
    // Ignore all cache entries by default
    return fetch(url, authentication, OffsetDateTime.MAX.toZonedDateTime());
  }

  String post(@NotNull String url,
              @NotNull Authentication authentication,
              @NotNull Map<String, Object> data) throws IOException;
}
