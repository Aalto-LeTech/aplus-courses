package fi.aalto.cs.apluscourses.ui;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;

public class CheckBoxTest {

  @Test
  public void testAddsItemListener() {
    CheckBox checkBox = new CheckBox();

    ObservableProperty<Boolean> isChecked = new ObservableReadWriteProperty<>(false);
    checkBox.isCheckedBindable.bindToSource(isChecked);

    ObservableProperty<Boolean> isEnabled = new ObservableReadWriteProperty<>(true);
    checkBox.isEnabledBindable.bindToSource(isEnabled);

    Assert.assertFalse("The check box should get its initial selection value from the given "
        + "observable property", checkBox.isSelected());
    Assert.assertTrue("The check box should get its initial enabled value from the given "
        + "observable property", checkBox.isEnabled());


    checkBox.setSelected(true);
    Assert.assertTrue("Selecting the check box should affect the given observable property",
        isChecked.get());

    checkBox.setSelected(false);
    Assert.assertFalse("Deselecting the check box should affect the given observable property",
        isChecked.get());
  }

  @Test
  public void testAddsValueObservers() {
    CheckBox checkBox = new CheckBox();

    ObservableProperty<Boolean> isChecked = new ObservableReadWriteProperty<>(false);
    checkBox.isCheckedBindable.bindToSource(isChecked);

    ObservableProperty<Boolean> isEnabled = new ObservableReadWriteProperty<>(true);
    checkBox.isEnabledBindable.bindToSource(isEnabled);

    String message
        = "Changes in the given observable properties should be reflected in the check box";

    isChecked.set(true);
    Assert.assertTrue(message, checkBox.isSelected());
    isChecked.set(false);
    Assert.assertFalse(message, checkBox.isSelected());

    isEnabled.set(true);
    Assert.assertTrue(message, checkBox.isEnabled());
    isEnabled.set(false);
    Assert.assertFalse(message, checkBox.isEnabled());
  }

  @Test
  public void testCallbackGetsCalled() {
    CheckBox checkBox = new CheckBox();

    ObservableProperty<Boolean> isChecked = new ObservableReadWriteProperty<>(true);

    ObservableProperty.Callback<CheckBox, Boolean> callback = mock(
        ObservableProperty.Callback.class);
    isChecked.addValueObserver(checkBox, callback);

    checkBox.setSelected(true);
    verify(callback).valueChanged(checkBox, true);
  }
}
