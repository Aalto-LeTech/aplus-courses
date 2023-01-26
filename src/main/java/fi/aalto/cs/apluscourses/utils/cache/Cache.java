package fi.aalto.cs.apluscourses.utils.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Cache<K, V> {
  @Nullable V getValue(K key, @NotNull CachePreference cachePreference);

  void putValue(K key, V value, @NotNull CachePreference cachePreference);

  void clearAll();
}
