package fi.aalto.cs.apluscourses.utils.content;

import fi.aalto.cs.apluscourses.utils.DirAwareZipFile;
import fi.aalto.cs.apluscourses.utils.RemoteFileCache;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public class RemoteZippedDir implements Content {
  private final String url;
  private final String path;
  private final RemoteFileCache remoteFileCache = RemoteFileCache.getInstance();

  public RemoteZippedDir(@NotNull String url, @NotNull String path) {
    this.url = url;
    this.path = path;
  }

  @Override
  public void copyTo(@NotNull Path destinationPath) throws IOException {
    new DirAwareZipFile(remoteFileCache.getCachedFile(url))
        .extractDir(path, destinationPath.toString());
  }
}
