package fi.aalto.cs.apluscourses.model.tutorial.switching;

import fi.aalto.cs.apluscourses.utils.FuncUtil;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwitchBase<S extends Switch<?>> implements Switch<S>  {
  private @Nullable S connection = null;

  protected <R> R delegate(@NotNull Function<? super S, R> func, R defaultValue) {
    return Optional.ofNullable(connection).map(func).orElse(defaultValue);
  }

  protected void delegate(@NotNull Consumer<? super S> action) {
    Optional.ofNullable(connection).ifPresent(action);
  }

  protected <T, R> R delegate(@NotNull BiFunction<? super S, T, R> func, T arg, R defaultValue) {
    return delegate(FuncUtil.bindSecond(func, arg), defaultValue);
  }

  protected <T> void delegate(@NotNull BiConsumer<? super S, T> action, T arg) {
    delegate(FuncUtil.bindSecond(action, arg));
  }

  @Override
  public void connect(@NotNull S connection) {
    this.connection = connection;
  }
}
