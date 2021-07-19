package fi.aalto.cs.apluscourses.utils.cache;

import java.time.Duration;
import org.jetbrains.annotations.NotNull;

public class CachePreference {

  public static final int DO_NOT_CACHE = 0;
  public static final int CACHE_FOR_SESSION = 1;
  public static final int LONG_TIME_CACHE = 2;

  public final @NotNull Duration allowedCacheAge;
  public final int cacheBehavior;

  public CachePreference(@NotNull Duration allowedCacheAge, int cacheBehavior) {
    this.allowedCacheAge = allowedCacheAge;
    this.cacheBehavior = cacheBehavior;
  }
}
