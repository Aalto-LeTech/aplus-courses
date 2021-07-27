package fi.aalto.cs.apluscourses.utils.cache;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A cache is essentially a map backed by a file. For each value, the time of creation is also
 * stored so that clients can determine the age of the cache entry. A subclass needs to implement
 * {@link FileCache#fromFile} and {@link FileCache#toFile}, which read and write the entries to a file. The
 * file format is completely up to the client. The file is read once when the cache is instantiated.
 * Writes occur after each insertion, but bursts of insertions only cause one write. Reading and
 * writing is done in a background thread, so a read or write may take a long time without blocking
 * other code. Note, that the cache makes no guarantee that every entry inserted is also written to
 * the file.
 */
public abstract class FileCache<K, V> extends CacheImpl<K, V> {

  private final File file;

  private final CountDownLatch fileRead = new CountDownLatch(1);

  private final AtomicBoolean writePending = new AtomicBoolean(false);

  private final ScheduledExecutorService writeExecutor
      = Executors.newSingleThreadScheduledExecutor();

  /**
   * Construct an instance with the given underlying file.
   */
  protected FileCache(@NotNull Path filePath) {
    this.file = filePath.toFile();
    new Thread(this::doInitialFileRead).start();
  }

  /**
   * Returns the entry corresponding to the given key, or null if no such entry exists.
   */
  @Override
  @Nullable
  public CacheEntry<V> getEntry(@NotNull K key) {
    ensureFileIsRead();
    return super.getEntry(key);
  }

  /**
   * Adds the given key and value to the cache. The creation time is determined by
   * {@link ZonedDateTime#now}. If an entry exists with the given key, it is replaced.
   */
  @Override
  protected void putEntry(@NotNull K key, @NotNull CacheEntry<V> entry) {
    ensureFileIsRead();
    super.putEntry(key, entry);
    queueFileWrite();
  }

  @NotNull
  protected abstract Map<K, CacheEntry<V>> fromFile(@NotNull File file) throws IOException;


  protected abstract void toFile(@NotNull File file,
                                 @NotNull Map<K, CacheEntry<V>> entries) throws IOException;

  private void doInitialFileRead() {
    try {
      setEntries(fromFile(file));
    } catch (IOException e) {
      setEntries(Collections.emptyMap());
    } finally {
      fileRead.countDown();
    }
  }

  private void writeFile() {
    Map<K, CacheEntry<V>> copy;
    synchronized (this) {
      copy = getEntries();
    }
    try {
      toFile(file, copy);
    } catch (IOException e) {
      // Ignore
    } finally {
      writePending.set(false);
    }
  }

  /*
   * Blocks until the file associated with this cache has been parsed. If the file has already been
   * read, then this method returns immediately.
   */
  private void ensureFileIsRead() {
    try {
      fileRead.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /*
   * If a write is already pending, does nothing. Otherwise creates a pending write, which runs in
   * the background after 10 seconds. This method can be called after each insertion into the cache.
   * The delay ensures that a burst of entries get coalesced into a single file write.
   */
  private void queueFileWrite() {
    if (writePending.compareAndSet(false, true)) {
      writeExecutor.schedule(this::writeFile, 10, TimeUnit.SECONDS);
    }
  }

}
