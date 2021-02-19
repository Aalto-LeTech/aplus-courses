package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.util.concurrency.annotations.RequiresReadLock;
import org.jetbrains.annotations.Nullable;

public interface IntelliJComponent<T> {
  @RequiresReadLock
  @Nullable
  T getPlatformObject();
}
