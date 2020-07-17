package fi.aalto.cs.apluscourses.utils;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FactorySelector<A, R> {
  private final ConcurrentHashMap<Class<?>, Factory<?, A, R>> factories = new ConcurrentHashMap<>();

  public <T> void register(@NotNull Class<T> klass, @NotNull Factory<? super T, A, R> factory) {
    factories.put(klass, factory);
  }

  @NotNull
  public <T> R create(T obj, A arg) {
    return Optional.ofNullable(createInternal(obj.getClass(), obj, arg))
        .orElseThrow(() -> new IllegalArgumentException("Cannot find suitable factory"));
  }

  @SuppressWarnings("unchecked")
  private <T> R createInternal(@Nullable Class<? extends T> klass, T obj, A arg) {
    if (klass == null) {
      return null;
    }
    Factory<T, A, R> factory = (Factory<T, A, R>) factories.get(klass);
    return factory == null
        ? createInternal(klass.getSuperclass(), obj, arg)
        : factory.create(obj, arg);
  }

  public interface Factory<T, A, R> {
    @NotNull
    R create(@NotNull T model, @Nullable A arg);
  }
}
