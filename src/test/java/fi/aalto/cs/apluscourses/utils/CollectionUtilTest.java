package fi.aalto.cs.apluscourses.utils;

import static fi.aalto.cs.apluscourses.OurMatchers.hasValue;
import static fi.aalto.cs.apluscourses.OurMatchers.isEmpty;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
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
  void testFindSingle() {
    assertThat(CollectionUtil.findSingle(Stream.empty()), isEmpty());
    assertThat(CollectionUtil.findSingle(Stream.of("alpha")), hasValue("alpha"));
    assertThat(CollectionUtil.findSingle(Stream.of("alpha", "beta")), isEmpty());
  }

  @Test
  void testGet() {
    final String[] alphabets = {
        //0    1    2    3    4    5    6    7    8    9
        "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
        "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
        "u", "v", "w", "x", "y", "z"
    };

    String[] vowels = CollectionUtil.get(Arrays.stream(alphabets), 0, 4, 8, 14, 20, 24).toArray(String[]::new);
    assertEquals("aeiouy", String.join("", vowels));

    String[] boost = CollectionUtil.get(Arrays.stream(alphabets), 1, 14, 14, 18, 19).toArray(String[]::new);
    assertEquals("boost", String.join("", boost));

    assertThrows(IllegalArgumentException.class, () -> CollectionUtil.get(Arrays.stream(alphabets), 1, 3, 2));
  }
}
