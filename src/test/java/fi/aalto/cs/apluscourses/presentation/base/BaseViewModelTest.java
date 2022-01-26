package fi.aalto.cs.apluscourses.presentation.base;

import static org.junit.Assert.assertSame;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BaseViewModelTest {

  @Test
  void testBaseViewModel() {
    Object model = new Object();
    BaseViewModel<?> baseViewModel = new BaseViewModel<>(model);
    Assertions.assertSame(model, baseViewModel.getModel(),
        "The model should be the same that was given to the constructor.");
  }
}
