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

  @SuppressWarnings("unchecked")
  @Test
  void testIndexAndListModel() {
    ListElementViewModel<Object> listElementViewModel = new ListElementViewModel<>(new Object());
    Assertions.assertEquals(0, listElementViewModel.getIndex());

    int index = 44;
    listElementViewModel.setIndex(index);
    Assertions.assertEquals(index, listElementViewModel.getIndex(), "getIndex() should return newly set index.");

    listElementViewModel.onChanged(); // nothing should happen

    BaseListViewModel<ListElementViewModel<Object>> listViewModel = mock(BaseListViewModel.class);
    listElementViewModel.setListModel(listViewModel);

    listElementViewModel.onChanged();
    verify(listViewModel).onElementChanged(index);

    verifyNoMoreInteractions(listViewModel);
  }
}
