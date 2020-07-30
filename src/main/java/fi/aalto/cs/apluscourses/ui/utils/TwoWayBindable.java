package fi.aalto.cs.apluscourses.ui.utils;

import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TwoWayBindable<T extends JComponent, V> extends Bindable<T, V> {

  private final Function<T, V> targetGetter;

  private ObservableProperty<V> sourceProperty;

  public TwoWayBindable(@NotNull T target,
                        @NotNull BiConsumer<T, V> targetSetter,
                        @NotNull Function<T, V> targetGetter,
                        V fallbackValue) {
    super(target, targetSetter, fallbackValue);
    this.targetGetter = targetGetter;
  }

  @Nullable
  @Override
  protected synchronized ObservableProperty<?> getSourceProperty() {
    return sourceProperty;
  }

  /**
   * Makes this bindable observe a source property and update the target accordingly.
   *
   * @param sourceProperty An {@link ObservableProperty} or null (to clear).
   */
  public synchronized void bindToSource(@Nullable ObservableProperty<V> sourceProperty) {
    clearProperty();
    bindInternal(sourceProperty);
    this.sourceProperty = sourceProperty;
  }

  /**
   * Updates the source property.
   */
  public synchronized void updateSource() {
    if (sourceProperty != null) {
      sourceProperty.set(targetGetter.apply(target));
    }
  }
}
