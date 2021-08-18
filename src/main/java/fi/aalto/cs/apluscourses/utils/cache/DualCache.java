package fi.aalto.cs.apluscourses.utils.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DualCache<K, V> implements Cache<K, V> {

  private final @NotNull Cache<K, V> longTimeCache;
  private final @NotNull Cache<K, V> shortTimeCache;

  public DualCache(@NotNull Cache<K, V> longTimeCache, @NotNull Cache<K, V> shortTimeCache) {
    this.longTimeCache = longTimeCache;
    this.shortTimeCache = shortTimeCache;
  }

  public DualCache(Cache<K, V> longTimeCache) {
    this(longTimeCache, new CacheImpl<>());
  }

  protected Cache<K, V> getAppropriateCache(@NotNull CachePreference cachePreference) {
    return cachePreference.cacheBehavior == CachePreference.LONG_TIME_CACHE ? longTimeCache : shortTimeCache;
  }

  @Override
  public @Nullable V getValue(K key, @NotNull CachePreference cachePreference) {
    return getAppropriateCache(cachePreference).getValue(key, cachePreference);
  }

  @Override
  public void putValue(K key, V value, @NotNull CachePreference cachePreference) {
    getAppropriateCache(cachePreference).putValue(key, value, cachePreference);
  }
}
