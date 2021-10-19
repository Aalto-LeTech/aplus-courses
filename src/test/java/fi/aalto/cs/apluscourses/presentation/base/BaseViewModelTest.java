package fi.aalto.cs.apluscourses.presentation.base;

import static org.junit.Assert.assertSame;

import org.junit.jupiter.api.Test;

public class BaseViewModelTest {
  
  @Test
  public void testBaseViewModel() {
    Object model = new Object();
    BaseViewModel<?> baseViewModel = new BaseViewModel<>(model);
    assertSame("The model should be the same that was given to the constructor.",
        model, baseViewModel.getModel());
  }
}
