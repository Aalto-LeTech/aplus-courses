package fi.aalto.cs.apluscourses.presentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProgressViewModelTest {

  @Test
  void testProgressViewModelTest() {
    var viewModel = new ProgressViewModel();
    var label = "hello";
    var maxValue = 2;
    viewModel.value.set(1);
    viewModel.indeterminate.set(false);
    var progress = viewModel.start(maxValue, label, true);
    Assertions.assertEquals(viewModel.getCurrentProgress(), progress, "The progress is the current progress");
    Assertions.assertEquals(label, viewModel.label.get(), "The label is correct when start is called");
    Assertions.assertEquals(Optional.of(0), Optional.ofNullable(viewModel.value.get()),
        "The value is correct when start is called");
    Assertions.assertEquals(Optional.of(maxValue), Optional.ofNullable(viewModel.maxValue.get()),
        "The max value is correct when start is called");
    Assertions.assertEquals(true, viewModel.visible.get(), "The progress is visible when start is called");

    progress.increment();

    Assertions.assertEquals(Optional.of(1), Optional.ofNullable(viewModel.value.get()),
        "The value is changed when incremented");

    progress.finish();
    Assertions.assertEquals(false, viewModel.visible.get(), "The progress is not visible after stopping");

    viewModel.indeterminate.set(true);
    Assertions.assertEquals(true, viewModel.visible.get(), "The progress is visible when indeterminate");
  }

  @Test
  void testMultipleProgresses() {
    var viewModel = new ProgressViewModel();
    var label1 = "hello";
    var label2 = "bye";
    var maxValue = 3;
    var progress1 = viewModel.start(maxValue, label1, true);
    var progress2 = viewModel.start(maxValue, label2, true);
    Assertions.assertNotEquals(viewModel.getCurrentProgress(), progress1,
        "The most recent progress is the current progress");
    Assertions.assertEquals(viewModel.getCurrentProgress(), progress2,
        "The most recent progress is the current progress");
    Assertions.assertEquals(Optional.of(0), Optional.ofNullable(viewModel.value.get()),
        "The value is correct when start is called");
    progress1.increment();
    progress1.increment();
    Assertions.assertEquals(Optional.of(0), Optional.ofNullable(viewModel.value.get()),
        "The value doesn't change if the incremented progress isn't the current one");
    progress2.increment();
    Assertions.assertEquals(Optional.of(1), Optional.ofNullable(viewModel.value.get()),
        "The value changes when the current progress is incremented");
    progress2.finish();
    Assertions.assertEquals(Optional.of(2), Optional.ofNullable(viewModel.value.get()),
        "The value changes when the current progress is changed");
  }
}
