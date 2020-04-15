package fi.aalto.cs.apluscourses.utils;

import java.util.stream.Stream;
import org.junit.Test;

public class StreamUtilTest {

  @Test(expected = InterruptedException.class)
  public void testForEach() throws InterruptedException {
    Stream<Integer> stream = Stream.of(2, 1, 0, -1, -2);
    StreamUtil.forEach(stream, this::interruptIfZero, InterruptedException.class);
  }

  private void interruptIfZero(int i) throws InterruptedException {
    if (i == 0) {
      throw new InterruptedException();
    }
  }
}
