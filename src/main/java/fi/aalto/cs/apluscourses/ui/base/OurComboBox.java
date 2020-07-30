package fi.aalto.cs.apluscourses.ui.base;

import com.intellij.openapi.ui.ComboBox;
import fi.aalto.cs.apluscourses.ui.utils.TwoWayBindable;
import org.jetbrains.annotations.Nullable;

public class OurComboBox<E> extends ComboBox<E> {
  private final Class<E> klass;

  public final TwoWayBindable<OurComboBox<E>, E> selectedItemBindable = new TwoWayBindable<>(this,
      OurComboBox::setSelectedItem, OurComboBox::getSelectedItemTyped, null);

  /**
   * A constructor.
   *
   * @param items An array.
   * @param klass E.class.
   */
  public OurComboBox(E[] items, Class<E> klass) {
    super(items);
    this.klass = klass;
    addItemListener(e -> selectedItemBindable.updateSource());
  }

  @Nullable
  private E getSelectedItemTyped() {
    Object item = getSelectedItem();
    return klass.isInstance(item) ? klass.cast(item) : null;
  }
}
