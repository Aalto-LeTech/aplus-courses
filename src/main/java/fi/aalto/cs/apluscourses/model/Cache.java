package fi.aalto.cs.apluscourses.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.HashMap;
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
 * {@link Cache#toFile} and {@link Cache#fromFile}, which read and write the entries to a file. The
 * file format is completely up to the client. The file is read once when the cache is instantiated.
 * Writes occur after each insertion, but bursts of insertions only cause one write. Reading and
 * writing is done in a background thread, so a read or write may take a long time without blocking
 * other code. Note, that the cache makes no guarantee that every entry inserted is also written to
 * the file.
 */
public abstract class Cache<KeyT, ValueT> {

  public class Entry {
    @NotNull
    private final ValueT value;

    @NotNull
    private final ZonedDateTime createdAt;

    public Entry(@NotNull ValueT value, @NotNull ZonedDateTime createdAt) {
      this.value = value;
      this.createdAt = createdAt;
    }

    public Entry(@NotNull ValueT value) {
      this(value, ZonedDateTime.now());
    }

    @NotNull
    public ValueT getValue() {
      return value;
    }

    @NotNull
    public ZonedDateTime getCreationTime() {
      return createdAt;
    }
  }

  private final File file;

  private Map<KeyT, Entry> entries;

  private final CountDownLatch fileRead = new CountDownLatch(1);

  private final AtomicBoolean writePending = new AtomicBoolean(false);

  private final ScheduledExecutorService writeExecutor
      = Executors.newSingleThreadScheduledExecutor();

  /**
   * Construct an instance with the given underlying file.
   */
  public Cache(@NotNull Path filePath) {
    this.file = filePath.toFile();
    new Thread(this::initialFileRead).start();
  }

  /**
   * Returns the entry corresponding to the given key, or null if no such entry exists.
   */
  @Nullable
  public synchronized Entry getEntry(@NotNull KeyT key) {
    ensureFileIsRead();
    return entries.get(key);
  }

  /**
   * Adds the given key and value to the cache. The creation time is determined by
   * {@link ZonedDateTime#now}. If an entry exists with the given key, it is replaced.
   */
  public synchronized void putValue(@NotNull KeyT key, @NotNull ValueT value) {
    ensureFileIsRead();
    entries.put(key, new Entry(value));
    queueFileWrite();
  }

  @NotNull
  protected abstract Map<KeyT, Entry> fromFile(@NotNull File file) throws IOException;

  @NotNull
  protected abstract void toFile(@NotNull File file,
                                 @NotNull Map<KeyT, Entry> entries) throws IOException;

  private void initialFileRead() {
    try {
      entries = fromFile(file);
      System.out.println("File successfully read");
    } catch (IOException e) {
      entries = new HashMap<>();
    } finally {
      fileRead.countDown();
    }
  }

  private void writeFile() {
    Map<KeyT, Entry> copy;
    synchronized (this) {
      copy = new HashMap<>(entries);
    }
    try {
      System.out.println("Beginning file write");
      toFile(file, copy);
      System.out.println("File write complete");
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
   * the background after 10 seconds. This method can be called after each insertion into the cache;
   * the delay ensures that a burst of entries get coalesced into a single file write.
   */
  private void queueFileWrite() {
    if (writePending.compareAndSet(false, true)) {
      System.out.println("Write queued");
      // The write delay ensures that multiple writes within a 10 second window (caused by frequent
      // insertions) get coalesced into a single write.
      writeExecutor.schedule(this::writeFile, 10, TimeUnit.SECONDS);
    } else {
      System.out.println("Write already queued");
    }
  }

}
