package fi.aalto.cs.apluscourses.presentation.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

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

    ListElementViewModel<Object> listElementViewModel1 = new ListElementViewModel<>(model1);
    ListElementViewModel<Object> listElementViewModel2 = new ListElementViewModel<>(model2);
    ListElementViewModel<Object> listElementViewModel3 = new ListElementViewModel<>(model3);

    Map<Object, ListElementViewModel<Object>> elementMap = new HashMap<>();
    elementMap.put(model1, listElementViewModel1);
    elementMap.put(model2, listElementViewModel2);
    elementMap.put(model3, listElementViewModel3);

    BaseListViewModel<ListElementViewModel<Object>> listViewModel =
        new BaseListViewModel<>(models, elementMap::get);

    assertSame("The first element should be accessible with index 0",
        listElementViewModel1, listViewModel.getElementAt(0));
    assertSame("The second element should be accessible with index 1",
        listElementViewModel2, listViewModel.getElementAt(1));
    assertSame("The third element should be accessible with index 2",
        listElementViewModel3, listViewModel.getElementAt(2));

    assertEquals("The first element should have index 0",
        0, listElementViewModel1.getIndex());
    assertEquals("The second element should have index 1",
        1, listElementViewModel2.getIndex());
    assertEquals("The third element should have index 2",
        2, listElementViewModel3.getIndex());

    assertSame("The list model of the first element should be set",
        listViewModel, listElementViewModel1.getListModel());
    assertSame("The list model of the second element should be set",
        listViewModel, listElementViewModel1.getListModel());
    assertSame("The list model of the third element should be set",
        listViewModel, listElementViewModel1.getListModel());
  }
}
