package fi.aalto.cs.apluscourses.presentation.filter;

public abstract class TypedFilter<T> implements Filter {

  private final Class<T> klass;

  public TypedFilter(Class<T> klass) {
    this.klass = klass;
  }

  @Override
  public boolean apply(Object item) {
    return klass.isInstance(item) && applyInternal(klass.cast(item));
  }

  public abstract boolean applyInternal(T item);
}
