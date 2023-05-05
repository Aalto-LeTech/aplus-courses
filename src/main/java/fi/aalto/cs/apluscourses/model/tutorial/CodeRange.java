package fi.aalto.cs.apluscourses.model.tutorial;

import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

public abstract class CodeRange {

  public static CodeRange empty() {
    return new EmptyCodeRange();
  }

  public abstract int getStartInclusive();

  public abstract int getEndExclusive();

  public boolean contains(int offset) {
    return getStartInclusive() <= offset && offset <= getEndExclusive();
  }

  private static class ActualCodeRange extends CodeRange {
    private final int first;
    private final int last;

    public ActualCodeRange(int first, int last) {
      if (first <= 0 || last < first) {
        throw new IllegalArgumentException();
      }
      this.first = first;
      this.last = last;
    }

    @Override
    public int getStartInclusive() {
      return first;
    }

    @Override
    public int getEndExclusive() {
      return last;
    }
  }

  private static class EmptyCodeRange extends CodeRange {
    @Override
    public int getStartInclusive() {
      return 0;
    }

    @Override
    public int getEndExclusive() {
      return 0;
    }

    @Override
    public boolean contains(int line) {
      return false;
    }
  }
}
