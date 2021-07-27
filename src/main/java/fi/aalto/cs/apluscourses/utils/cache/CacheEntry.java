package fi.aalto.cs.apluscourses.utils.cache;

import java.time.ZonedDateTime;
import org.jetbrains.annotations.NotNull;

public class CacheEntry<V> {
  @NotNull
  private final V value;

  @NotNull
  private final ZonedDateTime createdAt;

  public CacheEntry(@NotNull V value, @NotNull ZonedDateTime createdAt) {
    this.value = value;
    this.createdAt = createdAt;
  }

  public CacheEntry(@NotNull V value) {
    this(value, ZonedDateTime.now());
  }

  @NotNull
  public V getValue() {
    return value;
  }

  @NotNull
  public ZonedDateTime getCreationTime() {
    return createdAt;
  }
}
