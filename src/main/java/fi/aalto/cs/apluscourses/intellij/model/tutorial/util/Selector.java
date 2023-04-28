package fi.aalto.cs.apluscourses.intellij.model.tutorial.util;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public interface Selector<E> {
  @NotNull Optional<? extends E> select(@NotNull E root);
}
