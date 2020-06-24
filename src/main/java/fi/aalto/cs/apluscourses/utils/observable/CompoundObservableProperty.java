package fi.aalto.cs.apluscourses.utils.observable;

import java.util.function.BiFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An observable property that depends on two other observable properties. A compound observable
 * property is given a function that determines the value from the two observable properties that it
 * is dependent upon.
 *
 * @param <T1> The type contained in the first dependency observable property.
 * @param <T2> The type contained in the second dependency observable property.
 * @param <T>  The type contained in this observable property.
 */
public class CompoundObservableProperty<T1, T2, T> extends ObservableProperty<T> {

  @NotNull
  private final ObservableProperty<T1> property1;

  @NotNull
  private final ObservableProperty<T2> property2;

  @NotNull
  private final BiFunction<T1, T2, T> converter;

  /**
   * Construct a compound observable property that is dependent on the two given observable
   * properties. The value of this property (obtained using {@link CompoundObservableProperty#get}
   * is determined by {@code converter.apply(property1.get(), property2.get())}.
   *
   * @param property1 The first dependency property.
   * @param property2 The second dependency property.
   * @param converter A function that computes the value of this property from values of the
   *                  dependency properties.
   */
  public CompoundObservableProperty(@NotNull ObservableProperty<T1> property1,
                                    @NotNull ObservableProperty<T2> property2,
                                    @NotNull BiFunction<T1, T2, T> converter) {
    this.property1 = property1;
    this.property2 = property2;
    this.converter = converter;

    declareDependentOn(property1);
    declareDependentOn(property2);
  }

  @Nullable
  @Override
  public T get() {
    return converter.apply(property1.get(), property2.get());
  }
}
