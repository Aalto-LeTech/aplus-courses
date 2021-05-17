package fi.aalto.cs.apluscourses.presentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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
    var progress = viewModel.start(maxValue, label, true);
    assertEquals("The progress is the current progress", viewModel.getCurrentProgress(), progress);
    assertEquals("The label is correct when start is called", label, viewModel.label.get());
    assertEquals("The value is correct when start is called",
        Optional.of(0), Optional.ofNullable(viewModel.value.get()));
    assertEquals("The max value is correct when start is called",
        Optional.of(maxValue), Optional.ofNullable(viewModel.maxValue.get()));
    assertEquals("The progress is visible when start is called",
        true, viewModel.visible.get());

    progress.increment();

    assertEquals("The value is changed when incremented",
        Optional.of(1), Optional.ofNullable(viewModel.value.get()));

    progress.finish();
    assertEquals("The progress is not visible after stopping",
        false, viewModel.visible.get());

    viewModel.indeterminate.set(true);
    assertEquals("The progress is visible when indeterminate",
        true, viewModel.visible.get());
  }

  @Test
  public void testMultipleProgresses() {
    var viewModel = new ProgressViewModel();
    var label1 = "hello";
    var label2 = "bye";
    var maxValue = 3;
    var progress1 = viewModel.start(maxValue, label1, true);
    var progress2 = viewModel.start(maxValue, label2, true);
    assertNotEquals("The most recent progress is the current progress",
            viewModel.getCurrentProgress(), progress1);
    assertEquals("The most recent progress is the current progress",
            viewModel.getCurrentProgress(), progress2);
    assertEquals("The value is correct when start is called",
            Optional.of(0), Optional.ofNullable(viewModel.value.get()));
    progress1.increment();
    progress1.increment();
    assertEquals("The value doesn't change if the incremented progress isn't the current one",
            Optional.of(0), Optional.ofNullable(viewModel.value.get()));
    progress2.increment();
    assertEquals("The value changes when the current progress is incremented",
            Optional.of(1), Optional.ofNullable(viewModel.value.get()));
    progress2.finish();
    assertEquals("The value changes when the current progress is changed",
            Optional.of(2), Optional.ofNullable(viewModel.value.get()));
  }
}
