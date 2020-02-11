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
import org.junit.Test;

public class BaseListViewModelTest {

  @Test
  public void testCreateBaseListViewModel() {
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

    assertSame("The first element should be accessible with index 0.",
        listElementViewModel1, listViewModel.getElementAt(0));
    assertSame("The second element should be accessible with index 1.",
        listElementViewModel2, listViewModel.getElementAt(1));
    assertSame("The third element should be accessible with index 2.",
        listElementViewModel3, listViewModel.getElementAt(2));

    assertEquals("The first element should have index 0.",
        0, listElementViewModel1.getIndex());
    assertEquals("The second element should have index 1.",
        1, listElementViewModel2.getIndex());
    assertEquals("The third element should have index 2.",
        2, listElementViewModel3.getIndex());

    assertSame("The list model of the first element should be set.",
        listViewModel, listElementViewModel1.getListModel());
    assertSame("The list model of the second element should be set.",
        listViewModel, listElementViewModel1.getListModel());
    assertSame("The list model of the third element should be set.",
        listViewModel, listElementViewModel1.getListModel());
  }

  @Test
  public void testBaseListViewModelSelection() {
    List<Object> models = new ArrayList<>();
    models.add(new Object());
    models.add(new Object());
    models.add(new Object());

    BaseListViewModel<ListElementViewModel<?>> listViewModel =
        new BaseListViewModel<>(models, ListElementViewModel::new);

    ListElementViewModel<?> listElementViewModel1 = listViewModel.getElementAt(0);
    ListElementViewModel<?> listElementViewModel2 = listViewModel.getElementAt(1);
    ListElementViewModel<?> listElementViewModel3 = listViewModel.getElementAt(2);

    assertNotNull("1st element should not be null.", listElementViewModel1);
    assertNotNull("2nd element should not be null.", listElementViewModel2);
    assertNotNull("3rd element should not be null.", listElementViewModel3);

    assertFalse("1st element should not be initially selected.",
        listElementViewModel1.isSelected());
    assertFalse("2nd element should not be initially selected.",
        listElementViewModel2.isSelected());
    assertFalse("3rd element should not be initially selected.",
        listElementViewModel3.isSelected());

    listViewModel.getSelectionModel().addSelectionInterval(0, 0);
    listViewModel.getSelectionModel().addSelectionInterval(2, 2);

    assertTrue("1st element should be selected.", listElementViewModel1.isSelected());
    assertFalse("2nd element should not be selected.", listElementViewModel2.isSelected());
    assertTrue("3rd element should be selected.", listElementViewModel3.isSelected());

    List<ListElementViewModel<?>> selectedElements = listViewModel.getSelectedElements();

    assertEquals("Exactly 2 elements should be selected.",
        2, selectedElements.size());
    assertThat("1st and 3rd elements should be selected.",
        selectedElements, hasItems(listElementViewModel1, listElementViewModel3));
  }
}
