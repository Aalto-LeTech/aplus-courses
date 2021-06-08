package fi.aalto.cs.apluscourses.utils.content;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.utils.RemoteFileCache;
import java.io.IOException;
import java.nio.file.Path;
import net.lingala.zip4j.ZipFile;
import org.jetbrains.annotations.NotNull;

public class RemoteZippedFile implements Content {
  private final String url;
  private final String path;
  private final RemoteFileCache remoteFileCache = RemoteFileCache.getInstance();
  private final Project project;

  /**
   * Constructor.
   */
  public RemoteZippedFile(@NotNull String url, @NotNull String path, @NotNull Project project) {
    this.url = url;
    this.path = path;
    this.project = project;
  }

  @Override
  public void copyTo(@NotNull Path destinationPath) throws IOException {
    new ZipFile(remoteFileCache.getCachedFile(url, project))
        .extractFile(path, destinationPath.toString(), path.substring(path.lastIndexOf('/') + 1));
  }
}
