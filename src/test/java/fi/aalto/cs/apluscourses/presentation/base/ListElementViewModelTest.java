package fi.aalto.cs.apluscourses.presentation.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ListElementViewModelTest {

  @Test
  void testSetSelected() {
    ListElementViewModel<Object> listElementViewModel = new ListElementViewModel<>(new Object());
    Assertions.assertFalse(listElementViewModel.isSelected(), "isSelected() should initially return false.");
    listElementViewModel.setSelected(true);
    Assertions.assertTrue(listElementViewModel.isSelected(), "isSelected() should return true.");
    listElementViewModel.setSelected(false);
    Assertions.assertFalse(listElementViewModel.isSelected(), "isSelected() should return false.");
  }
}
