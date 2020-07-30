package fi.aalto.cs.apluscourses.ui.utils;

import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import java.util.function.BiConsumer;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OneWayBindable<T extends JComponent, S> extends Bindable<T, S> {

  private ObservableProperty<? extends S> sourceProperty;

  public OneWayBindable(@NotNull T target,
                        @NotNull BiConsumer<T, S> targetSetter,
                        S fallbackValue) {
    super(target, targetSetter, fallbackValue);
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
  public synchronized void bindToSource(@Nullable ObservableProperty<? extends S> sourceProperty) {
    clearProperty();
    bindInternal(sourceProperty);
    this.sourceProperty = sourceProperty;
  }
}
