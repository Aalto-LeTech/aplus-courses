package fi.aalto.cs.apluscourses.utils;

import java.util.function.Function;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public class Composite<E, C extends E> {
  private final @NotNull Class<C> compositeClass;
  private final @NotNull Function<@NotNull C, @NotNull Stream<@NotNull E>> childrenGetter;

  public Composite(@NotNull Class<C> compositeClass,
                   @NotNull Function<@NotNull C, @NotNull Stream<@NotNull E>> childrenGetter) {
    this.compositeClass = compositeClass;
    this.childrenGetter = childrenGetter;
  }

  public <T> Composite(@NotNull Class<C> compositeClass,
                       @NotNull Function<@NotNull C, @NotNull T> childrenGetter,
                       @NotNull Function<@NotNull T, @NotNull Stream<@NotNull E>> streamer) {
    this(compositeClass, childrenGetter.andThen(streamer));
  }

  public @NotNull Stream<@NotNull E> streamDescendants(@NotNull E root) {
    return compositeClass.isInstance(root)
        ? CollectionUtil.consStream(root,
                                    childrenGetter.apply(compositeClass.cast(root)).flatMap(this::streamDescendants))
        : Stream.of(root);
  }

  public @NotNull <T extends E> Stream<@NotNull T> streamDescendantsOfType(@NotNull Class<T> type, @NotNull E root) {
    return CollectionUtil.ofType(type, streamDescendants(root));
  }
}
