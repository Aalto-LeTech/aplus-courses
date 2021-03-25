package fi.aalto.cs.apluscourses.utils;

import com.intellij.openapi.util.io.FileUtilRt;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

public class RemoteFileCache {

  protected static final int DEFAULT_TIMEOUT_MILLIS = 5000;
  private static final RemoteFileCache INSTANCE = new RemoteFileCache();

  protected final int timeout;
  private final Map<String, CacheRecord> cache = new HashMap<>();

  protected RemoteFileCache() {
    this(DEFAULT_TIMEOUT_MILLIS);
  }

  /**
   * Instantiates a new remote file cache with a specified connection timeout.
   *
   * @param keepAliveTimeout After how many milliseconds, the download is timed out,
   *                         if a connection cannot be established or
   *                         if no new bytes cannot be fetched from the server.
   *
   *                         Note that downloading a large file can take much longer
   *                         than the value set here as long as there are
   *                         no pauses longer than the timeout specified.
   *
   *                         Refer to java.net.URLConnection.setConnectionTimeout
   *                         and java.net.URLConnection.setReadTimeout
   *                         for exact semantics.
   */
  protected RemoteFileCache(int keepAliveTimeout) {
    timeout = keepAliveTimeout;
  }

  public static RemoteFileCache getInstance() {
    return INSTANCE;
  }

  /**
   * Gets the file from the cache or downloads it, if it's not present.
   *
   * @param url URL for the (static) file.
   * @return A file.
   * @throws IOException If there were errors related to IO.
   */
  @NotNull
  public File getCachedFile(String url) throws IOException {
    CacheRecord record;
    synchronized (cache) {
      record = cache.get(url);
      if (record == null || record.isInvalid()) {
        record = new CacheRecord(FileUtilRt.createTempFile("", null), url);
        cache.put(url, record);
      }
    }
    return record.getFile();
  }

  private class CacheRecord {
    private final @NotNull File file;
    private final @NotNull String url;
    private boolean isDownloaded = false;
    private final Object lock = new Object();

    public CacheRecord(@NotNull File file, @NotNull String url) {
      this.file = file;
      this.url = url;
    }

    public boolean isInvalid() {
      return !file.exists();
    }

    public File getFile() throws IOException {
      synchronized (lock) {
        if (!isDownloaded) {
          FileUtils.copyURLToFile(new URL(url), file, timeout, timeout);
          isDownloaded = true;
        }
        return file;
      }
    }
  }
}
