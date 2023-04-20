package fi.aalto.cs.apluscourses.utils;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class that dispatches calls to function delegates using registered to it.  When {@code call()} method is called,
 * such a function delegate is used that is registered for the runtime type of the object (the first argument given to
 * {@code call()}).  The second parameter of {@code call()} is an additional parameter given to the factory.  If there
 * is no function registered for the exact runtime type of the given object, the superclass of that type is considered,
 * and then recursively until {@link Object}. If no function is registered for any of the supertypes, {@code call()}
 * method throws an {@link IllegalArgumentException}.
 *
 * Practically, this class can be used to dynamically attach extended functionality to classes.
 *
 * @param <A> Type of the additional parameter.
 * @param <R> The return type of functions.
 */
public class Dispatcher<A, R> {
  private final ConcurrentHashMap<Class<?>, FunctionDelegate<?, A, R>> functionDelegates = new ConcurrentHashMap<>();

  public <T> void register(@NotNull Class<T> klass, @NotNull FunctionDelegate<? super T, A, R> functionDelegate) {
    functionDelegates.put(klass, functionDelegate);
  }

  public R call(Object obj, A arg) {
    return callInternal(obj.getClass(), obj, arg);
  }

  @SuppressWarnings("unchecked")
  private <T> R callInternal(@Nullable Class<? extends T> klass, T obj, A arg) {
    if (klass == null) {
      throw new IllegalArgumentException("Cannot find suitable function delegate.");
    }
    FunctionDelegate<T, A, R> functionDelegate = (FunctionDelegate<T, A, R>) functionDelegates.get(klass);
    return functionDelegate == null
        ? callInternal(klass.getSuperclass(), obj, arg)
        : functionDelegate.callFor(obj, arg);
  }

  @FunctionalInterface
  public interface FunctionDelegate<T, A, R> {
    R callFor(@NotNull T obj, A arg);
  }
}
