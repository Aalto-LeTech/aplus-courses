package fi.aalto.cs.apluscourses.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CollectionUtil {
  private CollectionUtil() {

  }

  /**
   * Index-supporting mapping for lists.
   *
   * @param list       Source list.
   * @param func       Function from source element and its index to result element.
   * @param startIndex The beginning of indexing.
   * @param <T>        Type of source list elements.
   * @param <R>        Type of result list elements.
   * @return List of result elements.
   */
  public static <T, R> List<R> mapWithIndex(@NotNull List<T> list,
                                            @NotNull BiFunction<T, Integer, R> func,
                                            int startIndex) {
    return IntStream
        .range(0, list.size())
        .mapToObj(i -> func.apply(list.get(i), startIndex + i))
        .collect(Collectors.toList());
  }

  /**
   * Returns the index of the given element in an iterator.
   *
   * @param iterator   An iterator.
   * @param itemToFind A item that is wanted to be found.
   * @param <T>        Type of items.
   * @return The index of the item.
   */
  public static <T> int indexOf(@NotNull Iterator<T> iterator, @Nullable T itemToFind) {
    int index = 0;
    while (iterator.hasNext()) {
      T current = iterator.next();
      if (Objects.equals(current, itemToFind)) {
        return index;
      }
      index++;
    }
    return -1;
  }

  /**
   * Removes items for which the predicate is true.
   *
   * @param collection A collection.
   * @param predicate  A predicate that decides if an item is removed.
   * @param <T>        Type of items.
   */
  public static <T> Collection<T> removeIf(@NotNull Collection<T> collection,
                                           @NotNull Predicate<T> predicate) {
    List<T> toBeRemoved = new ArrayList<>();
    for (var item : collection) {
      if (predicate.test(item)) {
        toBeRemoved.add(item);
      }
    }
    collection.removeAll(toBeRemoved);
    return toBeRemoved;
  }

  /**
   * Returns the only element of the stream.
   *
   * @param stream A stream.
   * @param <E> Type of the elements in the stream.
   * @return If the stream contains only one element, it is returned. If there are no elements or more than one element,
   * empty is returned.
   */
  public static <E> Optional<E> findSingle(@NotNull Stream<E> stream) {
    var it = stream.iterator();
    if (!it.hasNext()) {
      return Optional.empty();
    }
    var elem = it.next();
    return it.hasNext() ? Optional.empty() : Optional.of(elem);
  }

  /**
   * Returns a sub-stream of the given stream with those elements indicated by indexStream.
   *
   * @param stream A stream.
   * @param indexStream An ascending stream of indices. An index can appear repeatedly.
   * @return A stream of those elements of the given stream that have indices indicated by the given indexStream.
   */
  public static <E> Stream<E> get(@NotNull Stream<E> stream, @NotNull LongStream indexStream) {
    long index = 0;
    Stream.Builder<E> result = Stream.builder();
    var iterator = stream.iterator();
    var indexIterator = indexStream.iterator();
    long expected = indexIterator.nextLong();
    while (iterator.hasNext()) {
      E current = iterator.next();
      while (true) {
        if (expected < index) {
          throw new IllegalArgumentException("Indices must be non-negative and in ascending order.");
        } else if (expected > index) {
          break;
        }
        result.accept(current);
        if (!indexIterator.hasNext()) {
          return result.build();
        }
        expected = indexIterator.nextLong();
      }
      index++;
    }
    return result.build();
  }

  public static <E> Stream<E> get(Stream<E> stream, long... indices) {
    return get(stream, Arrays.stream(indices));
  }
}
