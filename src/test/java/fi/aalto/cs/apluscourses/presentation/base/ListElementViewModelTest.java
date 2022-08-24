package fi.aalto.cs.apluscourses.presentation.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
