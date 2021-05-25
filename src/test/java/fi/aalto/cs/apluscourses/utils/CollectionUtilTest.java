package fi.aalto.cs.apluscourses.utils;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Test;

public class CollectionUtilTest {

  @Test
  public void testMapWithIndex() {
    List<String> source = List.of("a", "b", "c");
    List<String> result =
        CollectionUtil.mapWithIndex(source, (item, index) -> item + index.toString(), 4);
    assertThat(result, is(List.of("a4", "b5", "c6")));
  }

  @Test
  public void testIndexOf() {
    Object item = new Object();
    Iterator<Object> it = List.of(new Object(), new Object(), item, new Object()).iterator();
    assertEquals(2, CollectionUtil.indexOf(it, item));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testRemoveIf() {
    Consumer<String> callback = mock(Consumer.class);

    Collection<String> collection = new ArrayDeque<>();
    collection.add("Audi");
    collection.add("BMW");
    collection.add("Chevrolet");
    collection.add("Daimler");
    collection.add("Alfa Romeo");
    collection.add("Bentley");
    collection.add("Chrysler");
    collection.add("Dodge");

    var removed = CollectionUtil.removeIf(collection, s -> s.startsWith("C"));
    assertThat(removed, hasItem("Chevrolet"));
    assertThat(removed, hasItem("Chrysler"));

    assertEquals(6, collection.size());
    assertThat(collection, hasItem("Audi"));
    assertThat(collection, hasItem("BMW"));
    assertThat(collection, hasItem("Daimler"));
    assertThat(collection, hasItem("Alfa Romeo"));
    assertThat(collection, hasItem("Bentley"));
    assertThat(collection, hasItem("Dodge"));
  }
}
