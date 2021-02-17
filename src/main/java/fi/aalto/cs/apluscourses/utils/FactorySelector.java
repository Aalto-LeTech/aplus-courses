package fi.aalto.cs.apluscourses.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class that creates objects using factories that are registered to it.  When {@code create()}
 * method is called, such a factory is used that is registered for the runtime type of the object
 * (the first argument given to {@code create()}).  The second parameter of {@code create()} is an
 * additional parameter given to the factory.

 * If there is no factory registered for the exact runtime type of the given object, the superclass
 * of that type is considered, and then recursively until {@link Object}. If no factory is
 * registered for any of the supertypes, {@code create()} method throws an
 * {@link IllegalArgumentException}.
 *
 * @param <A> Type of the additional parameter.
 * @param <R> The type of the objects that are created by the factories.
 */
public class FactorySelector<A, R> {
  private final ConcurrentHashMap<Class<?>, Factory<?, A, R>> factories = new ConcurrentHashMap<>();

  public <T> void register(@NotNull Class<T> klass, @NotNull Factory<? super T, A, R> factory) {
    factories.put(klass, factory);
  }

  @NotNull
  public R create(Object obj, A arg) {
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
