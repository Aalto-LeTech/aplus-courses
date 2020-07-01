package fi.aalto.cs.apluscourses.ui;

import com.intellij.openapi.ui.ComboBox;
import fi.aalto.cs.apluscourses.utils.bindable.TwoWayBindable;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class OurComboBox<E> extends ComboBox<E> {
  private final Class<E> klass;

  public final TwoWayBindable<OurComboBox<E>, E> selectedItemBindable = new TwoWayBindable<>(this,
      OurComboBox::setSelectedItem, OurComboBox::getSelectedItemTyped);

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
