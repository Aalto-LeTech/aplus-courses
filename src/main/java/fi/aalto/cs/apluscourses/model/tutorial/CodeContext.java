package fi.aalto.cs.apluscourses.model.tutorial;

import java.nio.file.Path;
import org.jetbrains.annotations.Nullable;

public interface CodeContext {

  @Nullable Path getPath();

  int getStartInclusive();

  int getEndExclusive();

  default boolean contains(@Nullable Path path, int offset) {
    return concernsPath(path) && getStartInclusive() <= offset && offset < getEndExclusive();
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
