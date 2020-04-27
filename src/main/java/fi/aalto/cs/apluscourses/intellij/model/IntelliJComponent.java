package fi.aalto.cs.apluscourses.intellij.model;

import jdk.internal.jline.internal.Nullable;

public interface IntelliJComponent<T> {
  @Nullable
  T getPlatformObject();
}
