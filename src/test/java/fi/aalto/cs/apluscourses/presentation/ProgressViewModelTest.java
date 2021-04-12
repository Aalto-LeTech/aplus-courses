package fi.aalto.cs.apluscourses.presentation;

import static org.junit.Assert.assertEquals;

import java.util.Optional;
import org.junit.Test;

public class ProgressViewModelTest {

  @Test
  public void testProgressViewModelTest() {
    var viewModel = new ProgressViewModel();
    var label = "hello";
    var maxValue = 2;
    viewModel.value.set(1);
    viewModel.indeterminate.set(false);
    viewModel.start(maxValue, label);
    assertEquals("The label is correct when start is called", label, viewModel.label.get());
    assertEquals("The value is correct when start is called",
        Optional.of(0), Optional.ofNullable(viewModel.value.get()));
    assertEquals("The max value is correct when start is called",
        Optional.of(maxValue), Optional.ofNullable(viewModel.maxValue.get()));
    assertEquals("The progress is visible when start is called",
        true, viewModel.visible.get());

    viewModel.increment();
    viewModel.increment();

    assertEquals("The value is changed when incremented",
        Optional.of(maxValue), Optional.ofNullable(viewModel.value.get()));
    assertEquals("The progress is not visible when the value is the same as maxValue",
        false, viewModel.visible.get());

    viewModel.value.set(1);
    assertEquals(true, viewModel.visible.get());

    viewModel.stop();
    assertEquals("The progress is not visible after stopping",
        false, viewModel.visible.get());

    viewModel.indeterminate.set(true);
    assertEquals("The progress is visible when indeterminate",
        true, viewModel.visible.get());
  }
}
