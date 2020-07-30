package fi.aalto.cs.apluscourses.ui.utils;

import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ValidationError;
import java.util.Optional;
import java.util.function.BiConsumer;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Bindable<T extends JComponent, S> implements ValidationItem {
  @NotNull
  protected final T target;
  @NotNull
  protected final BiConsumer<T, S> targetSetter;

  private final S fallbackValue;

  /**
   * <p>Constructs a new {@link Bindable}.  Bindables can be used to bind UI components' properties
   * (e.g. the content of a text box) to an ObservableProperty of a view model (e.g. "first name"),
   * in a way that changes in one of them is automatically updated to another.</p>
   *
   * <p>Use {@link OneWayBindable} if you only want UI to reflect the state of the view model, and
   * {@link TwoWayBindable} if you also want UI to update the view model.</p>
   *
   * @param target        The target UI component.
   * @param targetSetter  A setter that sets the property of the target (e.g.
   *                      {@code JComponent::setVisible} if you want the visibility of the
   *                      component being determined by a boolean property of the view model).
   * @param fallbackValue Value that is passed to the target setter, if the view model is null.
   */
  public Bindable(@NotNull T target, @NotNull BiConsumer<T, S> targetSetter, S fallbackValue) {
    this.target = target;
    this.targetSetter = targetSetter;
    this.fallbackValue = fallbackValue;
    setToDefault();
  }

  private void setToDefault() {
    targetSetter.accept(target, fallbackValue);
  }

  protected void clearProperty() {
    ObservableProperty<?> sourceProperty = getSourceProperty();
    if (sourceProperty != null) {
      sourceProperty.removeValueObserver(this);
    }
  }

  protected void bindInternal(@Nullable ObservableProperty<? extends S> sourceProperty) {
    if (sourceProperty == null) {
      setToDefault();
    } else {
      sourceProperty.addValueObserver(target, targetSetter::accept);
    }
  }

  @Nullable
  protected abstract ObservableProperty<?> getSourceProperty();

  @Nullable
  @Override
  public ValidationError validate() {
    return Optional.ofNullable(getSourceProperty()).map(ObservableProperty::validate).orElse(null);
  }

  @Override
  public T getComponent() {
    return target;
  }
}
