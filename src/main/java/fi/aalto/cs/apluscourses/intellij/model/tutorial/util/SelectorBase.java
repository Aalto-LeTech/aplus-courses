package fi.aalto.cs.apluscourses.intellij.model.tutorial.util;

import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public abstract class SelectorBase<E> implements Selector<E> {
  private final @NotNull Selector<E> subSelector;

  public SelectorBase(@NotNull Selector<E> subSelector) {
    this.subSelector = subSelector;
  }

  protected abstract @NotNull Stream<? extends @NotNull E> stream(@NotNull E elem);

  @Override
  public @NotNull Optional<? extends E> select(@NotNull E root) {
    return stream(root)
        .map(subSelector::select)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  protected static class SelfSelector<E> implements Selector<E> {
    private static final Selector<?> INSTANCE = new SelfSelector<>();

    @SuppressWarnings("unchecked")
    public static <E> Selector<E> getInstance() {
      return (Selector<E>) INSTANCE;
    }

    @Override
    public @NotNull Optional<? extends E> select(@NotNull E root) {
      return Optional.of(root);
    }
  }
}
