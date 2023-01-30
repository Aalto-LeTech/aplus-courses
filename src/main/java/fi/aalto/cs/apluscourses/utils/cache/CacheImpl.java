package fi.aalto.cs.apluscourses.utils.cache;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CacheImpl<K, V> implements Cache<K, V> {
  private @NotNull Map<K, CacheEntry<V>> entries = new HashMap<>();
  private final @NotNull Object lock = new Object();
  private long expirationTimestamp = 0;

  @Nullable
  protected CacheEntry<V> getEntry(K key) {
    synchronized (lock) {
      return entries.get(key);
    }
  }

  protected void putEntry(K key, CacheEntry<V> entry) {
    synchronized (lock) {
      entries.put(key, entry);
    }
  }

  protected void setEntries(@NotNull Map<K, CacheEntry<V>> entries) {
    synchronized (lock) {
      this.entries = new HashMap<>(entries);
    }
  }

  protected @NotNull Map<K, CacheEntry<V>> getEntries() {
    synchronized (lock) {
      return new HashMap<>(entries);
    }
  }

  @Override
  public @Nullable V getValue(K key, @NotNull CachePreference cachePreference) {
    var entry = getEntry(key);
    return entry == null
        || entry.getCreationTime().plus(cachePreference.allowedCacheAge).isBefore(ZonedDateTime.now())
        || entry.getCreationTime().toEpochSecond() < expirationTimestamp
        ? null : entry.getValue();
  }

  @Override
  public void putValue(K key, V value, @NotNull CachePreference cachePreference) {
    if (cachePreference.cacheBehavior != CachePreference.DO_NOT_CACHE) {
      putEntry(key, new CacheEntry<>(value));
    }
  }

  @Override
  public void clearAll() {
    synchronized (lock) {
      entries.clear();
    }
  }

  @Override
  public void updateExpirationTimestamp(long expirationTimestamp) {
    this.expirationTimestamp = expirationTimestamp;
  }
}
