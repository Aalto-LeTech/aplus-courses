 package fi.aalto.cs.apluscourses.utils.content;

import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.utils.DirAwareZipFile;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

public class RemoteContent implements Content {
  private static final Map<String, File> CACHE = new HashMap<>();

  @NotNull
  private final String url;

  public RemoteContent(@NotNull String url) {
    this.url = url;
  }

  @NotNull
  protected File getCachedFile() throws IOException
  {
    synchronized (CACHE) {
      File file = CACHE.get(url);
      if (file == null || !file.exists()) {
        file = FileUtilRt.createTempFile("", null);
        FileUtils.copyURLToFile(new URL(url), file);
        CACHE.put(url, file);
      }
      return file;
    }
  }

  @Override
  public void copyTo(@NotNull Path destinationPath) throws IOException {
    FileUtils.copyFileToDirectory(getCachedFile(), destinationPath.toFile());
  }

  public static class Zipped extends RemoteContent {
    private final String path;

    public Zipped(@NotNull String url, @NotNull String path) {
      super(url);
      this.path = path;
    }

    @Override
    public void copyTo(@NotNull Path destinationPath) throws IOException {
      new DirAwareZipFile(getCachedFile()).extract(path, destinationPath.toString());
    }
  }
}
