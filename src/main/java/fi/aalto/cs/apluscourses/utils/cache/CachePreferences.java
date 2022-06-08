package fi.aalto.cs.apluscourses.utils.cache;

import java.time.Duration;

public class CachePreferences {

  public static final CachePreference PERMANENT =
      new CachePreference(Duration.ofDays(10000), CachePreference.LONG_TIME_CACHE);
  public static final CachePreference GET_MAX_ONE_WEEK_OLD =
      new CachePreference(Duration.ofDays(7), CachePreference.LONG_TIME_CACHE);
  public static final CachePreference FOR_THIS_SESSION_ONLY =
      new CachePreference(Duration.ofDays(1), CachePreference.CACHE_FOR_SESSION);
  public static final CachePreference GET_NEW_AND_KEEP =
      new CachePreference(Duration.ZERO, CachePreference.LONG_TIME_CACHE);
  public static final CachePreference GET_NEW_AND_FORGET =
      new CachePreference(Duration.ZERO, CachePreference.DO_NOT_CACHE);

  private CachePreferences() {

  }
}
