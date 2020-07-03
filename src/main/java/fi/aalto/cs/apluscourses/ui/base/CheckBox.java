package fi.aalto.cs.apluscourses.ui.base;

import fi.aalto.cs.apluscourses.utils.bindable.Bindable;
import fi.aalto.cs.apluscourses.utils.bindable.TwoWayBindable;
import javax.swing.JCheckBox;

/**
 * A class deriving from {@link JCheckBox} that works with the {@link TwoWayBindable} class to
 * manifest its selected state to the source property.
 */
public class CheckBox extends JCheckBox {

  public final transient TwoWayBindable<CheckBox, Boolean> isCheckedBindable =
      new TwoWayBindable<>(this, CheckBox::setSelected, CheckBox::isSelected);

  public final transient Bindable<CheckBox, Boolean> isEnabledBindable =
      new Bindable<>(this, CheckBox::setEnabled);

  public CheckBox() {
    addItemListener(e -> isCheckedBindable.updateSource());
  }
}
