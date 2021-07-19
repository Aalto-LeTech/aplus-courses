package fi.aalto.cs.apluscourses.utils.cache;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CacheImpl<K, V> implements Cache<K, V> {
  private volatile @NotNull Map<K, CacheEntry<V>> entries = new HashMap<>();

  @Nullable
  protected synchronized CacheEntry<V> getEntry(K key) {
    return entries.get(key);
  }

  protected synchronized void putEntry(K key, CacheEntry<V> entry) {
    entries.put(key, entry);
  }

  protected synchronized void setEntries(@NotNull Map<K, CacheEntry<V>> entries) {
    this.entries = new HashMap<>(entries);
  }

  protected synchronized @NotNull Map<K, CacheEntry<V>> getEntries() {
    return new HashMap<>(entries);
  }

  @Override
  public @Nullable V getValue(K key, @NotNull CachePreference cachePreference) {
    var entry = getEntry(key);
    return entry == null
        || entry.getCreationTime().plus(cachePreference.allowedCacheAge).isBefore(ZonedDateTime.now())
        ? null : entry.getValue();
  }

  @Override
  public void putValue(K key, V value, @NotNull CachePreference cachePreference) {
    if (cachePreference.cacheBehavior != CachePreference.DO_NOT_CACHE) {
      putEntry(key, new CacheEntry<>(value));
    }
  }
}
