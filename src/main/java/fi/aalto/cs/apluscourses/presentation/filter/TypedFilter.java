package fi.aalto.cs.apluscourses.presentation.filter;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public abstract class TypedFilter<T> implements Filter {

  private final Class<T> klass;

  protected TypedFilter(Class<T> klass) {
    this.klass = klass;
  }

  @Override
  @NotNull
  public Optional<Boolean> apply(Object item) {
    if (!klass.isInstance(item)) {
      return Optional.empty();
    }
    return Optional.of(applyInternal(klass.cast(item)));
  }

  public abstract boolean applyInternal(T item);
}
