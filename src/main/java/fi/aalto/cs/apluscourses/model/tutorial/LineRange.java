package fi.aalto.cs.apluscourses.model.tutorial;

import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

public abstract class LineRange {
  private static final Pattern PATTERN = Pattern.compile("([0-9]+)-([0-9]+)");

  public static @NotNull LineRange parse(@NotNull String lines) {
    var matcher = PATTERN.matcher(lines);
    return matcher.matches()
        ? between(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)))
        : from(Integer.parseInt(lines));
  }

  public static LineRange between(int first, int last) {
    return new ActualLineRange(first, last);
  }

  public static LineRange from(int line) {
    return new ActualLineRange(line, line);
  }

  public static LineRange empty() {
    return new EmptyLineRange();
  }

  public abstract int getFirst();

  public abstract int getLast();

  public IntStream stream() {
    return IntStream.range(getFirst(), getLast() + 1);
  }

  public int lineCount() {
    return getLast() - getFirst() + 1;
  }

  public boolean contains(int line) {
    return getFirst() <= line && line <= getLast();
  }

  private static class ActualLineRange extends LineRange {
    private final int first;
    private final int last;

    public ActualLineRange(int first, int last) {
      if (first <= 0 || last < first) {
        throw new IllegalArgumentException();
      }
      this.first = first;
      this.last = last;
    }

    @Override
    public int getFirst() {
      return first;
    }

    @Override
    public int getLast() {
      return last;
    }
  }

  private static class EmptyLineRange extends LineRange {
    @Override
    public int getFirst() {
      return 1;
    }

    @Override
    public int getLast() {
      return 0;
    }

    @Override
    public int lineCount() {
      return 0;
    }

    @Override
    public boolean contains(int line) {
      return false;
    }
  }
}
