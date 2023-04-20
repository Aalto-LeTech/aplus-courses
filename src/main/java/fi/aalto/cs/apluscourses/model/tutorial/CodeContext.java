package fi.aalto.cs.apluscourses.model.tutorial;

import java.nio.file.Path;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CodeContext {

  @Nullable Path getPath();

  @NotNull LineRange getLineRange();

  default boolean contains(@Nullable Path path, int line) {
    return concernsPath(path) && getLineRange().contains(line);
  }

  private boolean concernsPath(@Nullable Path path) {
    var myPath = getPath();
    if (myPath == null) {
      return true;
    }
    if (path == null) {
      return false;
    }
    return path.endsWith(myPath);
  }
}
