package fi.aalto.cs.apluscourses.utils.bindable;

import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.Nullable;

public class Bindable<T, S> {
  protected final T target;
  protected final BiConsumer<T, S> targetSetter;
  protected ObservableProperty<S> sourceProperty;

  public Bindable(T target, BiConsumer<T, S> targetSetter) {
    this.target = target;
    this.targetSetter = targetSetter;
  }

  /**
   * Makes this bindable observe a source property and update the target accordingly.
   *
   * @param sourceProperty An {@link ObservableProperty} or null (to clear).
   */
  public synchronized void bindToSource(@Nullable ObservableProperty<S> sourceProperty) {
    if (this.sourceProperty != null) {
      this.sourceProperty.removeValueObserver(this);
    }
    if (sourceProperty != null) {
      sourceProperty.addValueObserver(target, targetSetter::accept);
    }
    this.sourceProperty = sourceProperty;
  }
}
