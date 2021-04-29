package fi.aalto.cs.apluscourses.ui.utils;

import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ValidationError;
import java.util.Optional;
import java.util.function.BiConsumer;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Bindable<T extends JComponent, S> implements ValidationItem {
  @NotNull
  protected final T target;
  @NotNull
  protected final BiConsumer<T, S> targetSetter;
  @Nullable
  protected ObservableProperty<S> sourceProperty;

  public Bindable(@NotNull T target, @NotNull BiConsumer<T, S> targetSetter) {
    this.target = target;
    this.targetSetter = targetSetter;
  }

  /**
   * Constructor for Bindable, that sets the targetSetter to invokeLater.
   */
  public Bindable(@NotNull T target, @NotNull BiConsumer<T, S> targetSetter, boolean lateInvoke) {
    this.target = target;
    this.targetSetter = lateInvoke
            ? (t, s) -> SwingUtilities.invokeLater(() -> targetSetter.accept(t, s)) : targetSetter;
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

  @Nullable
  @Override
  public synchronized ValidationError validate() {
    return Optional.ofNullable(sourceProperty).map(ObservableProperty::validate).orElse(null);
  }

  @Override
  public T getComponent() {
    return target;
  }
}
