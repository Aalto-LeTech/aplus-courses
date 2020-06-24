package fi.aalto.cs.apluscourses.intellij.model;

import org.jetbrains.annotations.CalledWithReadLock;
import org.jetbrains.annotations.Nullable;

public interface IntelliJComponent<T> {
  @CalledWithReadLock
  @Nullable
  T getPlatformObject();
}
