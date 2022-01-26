package fi.aalto.cs.apluscourses.ui.base;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CheckBoxTest {

  @Test
  void testAddsItemListener() {
    CheckBox checkBox = new CheckBox();

    ObservableProperty<Boolean> isChecked = new ObservableReadWriteProperty<>(false);
    checkBox.isCheckedBindable.bindToSource(isChecked);

    ObservableProperty<Boolean> isEnabled = new ObservableReadWriteProperty<>(true);
    checkBox.isEnabledBindable.bindToSource(isEnabled);

    Assertions.assertFalse(checkBox.isSelected(), "The check box should get its initial selection value from the given "
        + "observable property");
    Assertions.assertTrue(checkBox.isEnabled(), "The check box should get its initial enabled value from the given "
        + "observable property");


    checkBox.setSelected(true);
    Assertions.assertTrue(isChecked.get(), "Selecting the check box should affect the given observable property");

    checkBox.setSelected(false);
    Assertions.assertFalse(isChecked.get(), "Deselecting the check box should affect the given observable property");
  }

  @Test
  void testAddsValueObservers() {
    CheckBox checkBox = new CheckBox();

    ObservableProperty<Boolean> isChecked = new ObservableReadWriteProperty<>(false);
    checkBox.isCheckedBindable.bindToSource(isChecked);

    ObservableProperty<Boolean> isEnabled = new ObservableReadWriteProperty<>(true);
    checkBox.isEnabledBindable.bindToSource(isEnabled);

    String message
        = "Changes in the given observable properties should be reflected in the check box";

    isChecked.set(true);
    Assertions.assertTrue(checkBox.isSelected(), message);
    isChecked.set(false);
    Assertions.assertFalse(checkBox.isSelected(), message);

    isEnabled.set(true);
    Assertions.assertTrue(checkBox.isEnabled(), message);
    isEnabled.set(false);
    Assertions.assertFalse(checkBox.isEnabled(), message);
  }

  @Test
  void testCallbackGetsCalled() {
    CheckBox checkBox = new CheckBox();

    ObservableProperty<Boolean> isChecked = new ObservableReadWriteProperty<>(true);

    ObservableProperty.Callback<CheckBox, Boolean> callback = mock(
        ObservableProperty.Callback.class);
    isChecked.addValueObserver(checkBox, callback);

    checkBox.setSelected(true);
    verify(callback).valueChanged(checkBox, true);
  }
}
