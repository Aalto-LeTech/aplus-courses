package fi.aalto.cs.apluscourses.ui;

import fi.aalto.cs.apluscourses.utils.ObservableProperty;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;

public class CheckBoxTest {

  @Test
  public void testAddsItemListener() {
    ObservableProperty<Boolean> isChecked = new ObservableProperty<>(false);
    ObservableProperty<Boolean> isEnabled = new ObservableProperty<>(true);
    CheckBox checkBox = new CheckBox("", isChecked, isEnabled);

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
    ObservableProperty<Boolean> isChecked = new ObservableProperty<>(false);
    ObservableProperty<Boolean> isEnabled = new ObservableProperty<>(false);
    CheckBox checkBox = new CheckBox("", isChecked, isEnabled);

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
  public void testItemListenerDoesNotSetValueAgain() {
    AtomicInteger callCount = new AtomicInteger(0);
    ObservableProperty<Boolean> isChecked = new ObservableProperty<>(true);
    ObservableProperty.ValueObserver<Boolean> isCheckedObserver
        = b -> callCount.incrementAndGet();
    isChecked.addValueObserver(isCheckedObserver);

    CheckBox checkBox = new CheckBox("test", isChecked, new ObservableProperty<>(true));
    checkBox.setSelected(true);
    Assert.assertEquals("The observable property shouldn't be set with its existing value",
        1, callCount.get());
  }
}
