package fi.aalto.cs.apluscourses.utils;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static org.apache.commons.io.FileUtils.openOutputStream;
import static org.apache.commons.io.IOUtils.DEFAULT_BUFFER_SIZE;
import static org.apache.commons.io.IOUtils.EOF;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Progress;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class RemoteFileCache {

  protected static final int DEFAULT_TIMEOUT_MILLIS = 120000;
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
   *                         Note that downloading a large file can take much longer
   *                         than the value set here as long as there are
   *                         no pauses longer than the timeout specified.
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
  public File getCachedFile(String url, @NotNull Project project) throws IOException {
    CacheRecord record;
    synchronized (cache) {
      record = cache.get(url);
      if (record == null || record.isInvalid()) {
        record = new CacheRecord(FileUtilRt.createTempFile("", null), url);
        cache.put(url, record);
      }
    }
    return record.getFile(project);
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

    public File getFile(@NotNull Project project) throws IOException {
      synchronized (lock) {
        if (!isDownloaded) {
          copyUrlToFile(new URL(url), file, timeout, timeout, project);
          isDownloaded = true;
        }
        return file;
      }
    }
  }

  private static void copyUrlToFile(@NotNull URL source,
                                    @NotNull File destination,
                                    int connectionTimeout,
                                    final int readTimeout,
                                    @NotNull Project project) throws IOException {
    var connection = source.openConnection();
    connection.setConnectTimeout(connectionTimeout);
    connection.setReadTimeout(readTimeout);
    var length = connection.getContentLengthLong();
    var progress = PluginSettings.getInstance().getMainViewModel(project).progressViewModel
        .start((int) (length / 1024),
            getAndReplaceText("ui.ProgressBarView.downloading", source.getFile()), false);
    try (var stream = connection.getInputStream();
         var out = openOutputStream(destination)) {
      copyLarge(stream, out, new byte[DEFAULT_BUFFER_SIZE], progress);
    } catch (IOException e) {
      new DefaultNotifier().notify(new NetworkErrorNotification(e), project);
      progress.finish();
      throw e;
    }
  }

  private static void copyLarge(@NotNull InputStream input,
                                @NotNull OutputStream output,
                                byte @NotNull [] buffer,
                                @NotNull Progress progress)
      throws IOException {
    int n;
    long total = 0;
    while (EOF != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
      total += n;
      progress.setValue((int) (total / 1024));
    }
    progress.finish();
  }
}
