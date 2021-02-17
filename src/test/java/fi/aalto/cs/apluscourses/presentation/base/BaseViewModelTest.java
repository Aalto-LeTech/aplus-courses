package fi.aalto.cs.apluscourses.presentation.base;

import org.junit.Test;

import static org.junit.Assert.assertSame;

public class BaseViewModelTest {
  
  @Test
  public void testBaseViewModel() {
    Object model = new Object();
    BaseViewModel<?> baseViewModel = new BaseViewModel<>(model);
    assertSame("The model should be the same that was given to the constructor.",
        model, baseViewModel.getModel());
  }
}
