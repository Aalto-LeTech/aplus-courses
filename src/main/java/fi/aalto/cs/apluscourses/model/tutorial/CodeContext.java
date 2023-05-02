package fi.aalto.cs.apluscourses.model.tutorial;

import java.nio.file.Path;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CodeContext {

  @Nullable Path getPath();

  int getStartOffset();

  int getEndOffset();

  default boolean contains(@Nullable Path path, int offset) {
    return concernsPath(path) && getStartOffset() < offset && offset < getEndOffset();
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
