package fi.aalto.cs.apluscourses.utils;

import static fi.aalto.cs.apluscourses.OurMatchers.isNegative;
import static fi.aalto.cs.apluscourses.OurMatchers.isPositive;
import static fi.aalto.cs.apluscourses.OurMatchers.isZero;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class CollectionUtilTest {

  @Test
  void testMapWithIndex() {
    List<String> source = List.of("a", "b", "c");
    List<String> result =
        CollectionUtil.mapWithIndex(source, (item, index) -> item + index.toString(), 4);
    assertThat(result, is(List.of("a4", "b5", "c6")));
  }

  @Test
  void testIndexOf() {
    Object item = new Object();
    Iterator<Object> it = List.of(new Object(), new Object(), item, new Object()).iterator();
    assertEquals(2, CollectionUtil.indexOf(it, item));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testRemoveIf() {
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

  @Test
  void testCompareLength() {
    String[] array = {"one", "two", "three"};
    assertThat(CollectionUtil.compareLength(0, Arrays.stream(array)), isNegative());
    assertThat(CollectionUtil.compareLength(2, Arrays.stream(array)), isNegative());
    assertThat(CollectionUtil.compareLength(3, Arrays.stream(array)), isZero());
    assertThat(CollectionUtil.compareLength(4, Arrays.stream(array)), isPositive());
    assertThat(CollectionUtil.compareLength(100, Arrays.stream(array)), isPositive());
    assertThat(CollectionUtil.compareLength(2, Arrays.stream(array).filter(s -> s.startsWith("t"))), isZero());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testCompareLengthShortCircuits() {
    var iterator = mock(Iterator.class);
    doReturn(true).when(iterator).hasNext();
    doReturn(null).when(iterator).next();
    var infiniteStream = ((Streamable<?>) () -> iterator).stream();

    assertThat(CollectionUtil.compareLength(10, infiniteStream), isNegative());

    verify(iterator, times(11)).hasNext();
  }
}
