package fi.aalto.cs.apluscourses.intellij.model;

import org.jetbrains.annotations.Nullable;

public interface IntelliJComponent<T> {
  @Nullable
  T getPlatformObject();
}
