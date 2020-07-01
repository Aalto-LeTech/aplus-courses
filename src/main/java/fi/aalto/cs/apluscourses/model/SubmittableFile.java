package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.intellij.utils.VfsUtil;
import java.nio.file.Path;
import java.util.Optional;
import jdk.internal.jline.internal.Nullable;

public class SubmittableFile {
  private final String name;

  public SubmittableFile(String name) {
    this.name = name;
  }

  @Nullable
  public Path getPath(Path basePath) throws FileDoesNotExistException {
    return Optional.ofNullable(VfsUtil.findFileInDirectory(basePath, name))
        .orElseThrow(() -> new FileDoesNotExistException(name, null));
  }

  public String getName() {
    return name;
  }
}
