package fi.aalto.cs.apluscourses.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class CommonUtilTest {

  @Test
  public void testCreateList() {
    Object item1 = new Object();
    Object item2 = new Object();
    Object item3 = new Object();
    Map<Integer, Object> itemMap = new HashMap<>();
    itemMap.put(0, item1);
    itemMap.put(1, item2);
    itemMap.put(2, item3);
    List<Object> items = CommonUtil.createList(3, itemMap::get);

    assertEquals("Size of the list should be 3.",
        3, items.size());
    assertSame("The first item should be the one with index 0.",
        item1, items.get(0));
    assertSame("The second item should be the one with index 1.",
        item2, items.get(1));
    assertSame("The third item should be the one with index 2.",
        item3, items.get(2));
  }
}
