package fi.aalto.cs.apluscourses.utils.bindable;

import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import java.util.function.BiConsumer;
import jdk.internal.jline.internal.Nullable;

public class Bindable<T, S> {
  protected final T target;
  protected final BiConsumer<T, S> targetSetter;
  protected ObservableProperty<S> sourceProperty;

  public Bindable(T target, BiConsumer<T, S> targetSetter) {
    this.target = target;
    this.targetSetter = targetSetter;
  }

  public synchronized void bindToSource(S value) {
    setSourcePropertyInternal(null);
    targetSetter.accept(target, value);
  }

  public synchronized void bindToSource(ObservableProperty<S> sourceProperty) {
    setSourcePropertyInternal(sourceProperty);
    sourceProperty.addValueObserver(target, targetSetter::accept);
  }

  private synchronized void setSourcePropertyInternal(
      @Nullable ObservableProperty<S> sourceProperty) {
    if (this.sourceProperty != null) {
      this.sourceProperty.removeValueObserver(this);
    }
    this.sourceProperty = sourceProperty;
  }
}
