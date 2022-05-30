package fi.aalto.cs.apluscourses.presentation.base;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BaseListViewModelTest {

  @Test
  void testCreateBaseListViewModel() {
    Object model1 = new Object();
    Object model2 = new Object();
    Object model3 = new Object();

    List<Object> models = new ArrayList<>();
    models.add(model1);
    models.add(model2);
    models.add(model3);

    ListElementViewModel<?> listElementViewModel1 = new ListElementViewModel<>(model1);
    ListElementViewModel<?> listElementViewModel2 = new ListElementViewModel<>(model2);
    ListElementViewModel<?> listElementViewModel3 = new ListElementViewModel<>(model3);

    Map<Object, ListElementViewModel<?>> elementMap = new HashMap<>();
    elementMap.put(model1, listElementViewModel1);
    elementMap.put(model2, listElementViewModel2);
    elementMap.put(model3, listElementViewModel3);

    BaseListViewModel<ListElementViewModel<?>> listViewModel =
        new BaseListViewModel<>(models, elementMap::get);

    Assertions.assertSame(listElementViewModel1, listViewModel.getElementAt(0),
        "The first element should be accessible with index 0.");
    Assertions.assertSame(listElementViewModel2, listViewModel.getElementAt(1),
        "The second element should be accessible with index 1.");
    Assertions.assertSame(listElementViewModel3, listViewModel.getElementAt(2),
        "The third element should be accessible with index 2.");

    Assertions.assertEquals(0, listElementViewModel1.getIndex(), "The first element should have index 0.");
    Assertions.assertEquals(1, listElementViewModel2.getIndex(), "The second element should have index 1.");
    Assertions.assertEquals(2, listElementViewModel3.getIndex(), "The third element should have index 2.");
  }

  @Test
  void testBaseListViewModelSelection() {
    List<Object> models = new ArrayList<>();
    models.add(new Object());
    models.add(new Object());
    models.add(new Object());

    BaseListViewModel<ListElementViewModel<?>> listViewModel =
        new BaseListViewModel<>(models, null, ListElementViewModel::new);

    ListElementViewModel<?> listElementViewModel1 = listViewModel.getElementAt(0);
    ListElementViewModel<?> listElementViewModel2 = listViewModel.getElementAt(1);
    ListElementViewModel<?> listElementViewModel3 = listViewModel.getElementAt(2);

    Assertions.assertNotNull(listElementViewModel1, "1st element should not be null.");
    Assertions.assertNotNull(listElementViewModel2, "2nd element should not be null.");
    Assertions.assertNotNull(listElementViewModel3, "3rd element should not be null.");

    Assertions.assertFalse(listElementViewModel1.isSelected(), "1st element should not be initially selected.");
    Assertions.assertFalse(listElementViewModel2.isSelected(), "2nd element should not be initially selected.");
    Assertions.assertFalse(listElementViewModel3.isSelected(), "3rd element should not be initially selected.");

    listViewModel.getSelectionModel().addSelectionInterval(0, 0);
    listViewModel.getSelectionModel().addSelectionInterval(2, 2);

    Assertions.assertTrue(listElementViewModel1.isSelected(), "1st element should be selected.");
    Assertions.assertFalse(listElementViewModel2.isSelected(), "2nd element should not be selected.");
    Assertions.assertTrue(listElementViewModel3.isSelected(), "3rd element should be selected.");

    List<ListElementViewModel<?>> selectedElements = listViewModel.getSelectedElements();

    Assertions.assertEquals(2, selectedElements.size(), "Exactly 2 elements should be selected.");
    assertThat("1st and 3rd elements should be selected.",
        selectedElements, hasItems(listElementViewModel1, listElementViewModel3));
  }
}
