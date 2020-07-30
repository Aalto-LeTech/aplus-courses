package fi.aalto.cs.apluscourses.ui.base;

import fi.aalto.cs.apluscourses.ui.utils.Bindable;
import fi.aalto.cs.apluscourses.ui.utils.OneWayBindable;
import fi.aalto.cs.apluscourses.ui.utils.TwoWayBindable;
import javax.swing.JCheckBox;

/**
 * A class deriving from {@link JCheckBox} that works with the {@link TwoWayBindable} class to
 * manifest its selected state to the source property.
 */
public class CheckBox extends JCheckBox {

  public final transient TwoWayBindable<CheckBox, Boolean> isCheckedBindable =
      new TwoWayBindable<>(this, CheckBox::setSelected, CheckBox::isSelected, false);

  public final transient OneWayBindable<CheckBox, Boolean> isEnabledBindable =
      new OneWayBindable<>(this, CheckBox::setEnabled, false);

  public CheckBox() {
    addItemListener(e -> isCheckedBindable.updateSource());
  }
}
