package fi.aalto.cs.apluscourses.ui.utils;

import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.swing.JComponent;

public class TwoWayBindable<T extends JComponent, V> extends Bindable<T, V> {

  private final Function<T, V> targetGetter;

  public TwoWayBindable(T target, BiConsumer<T, V> targetSetter, Function<T, V> targetGetter) {
    super(target, targetSetter);
    this.targetGetter = targetGetter;
  }

  /**
   * Updates the source property.
   */
  public synchronized void updateSource() {
    if (sourceProperty != null) {
      sourceProperty.set(targetGetter.apply(target), target);
    }
  }
}
