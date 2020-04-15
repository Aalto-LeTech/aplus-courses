package fi.aalto.cs.apluscourses.utils;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class StreamUtil {
  public static <T, E extends Exception> void forEach(Stream<T> stream,
                                                      ThrowingConsumer<T, E> consumer,
                                                      Class<E> exceptionClass) throws E {
    try {
      stream.forEach(new ConsumerWrapper<T, E>(consumer));
    } catch (ExceptionWrapper e) {
      Throwable cause = e.getCause();
      throw exceptionClass.cast(cause);
    }
  }

  private static class ConsumerWrapper<T, E extends Exception> implements Consumer<T> {
    private final ThrowingConsumer<T, E> throwingConsumer;

    public ConsumerWrapper(ThrowingConsumer<T, E> throwingConsumer) {
      this.throwingConsumer = throwingConsumer;
    }

    @Override
    public void accept(T t) {
      try {
        throwingConsumer.accept(t);
      } catch (Exception e) {
        throw new ExceptionWrapper(e);
      }
    }
  }

  private static class ExceptionWrapper extends RuntimeException {
    public ExceptionWrapper(Exception cause) {
      super(cause);
    }
  }

  public interface ThrowingConsumer<T, E extends Exception> {
    void accept(T t) throws E;
  }
}
