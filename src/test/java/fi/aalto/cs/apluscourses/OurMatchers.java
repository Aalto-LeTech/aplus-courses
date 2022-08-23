package fi.aalto.cs.apluscourses;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class OurMatchers {
  private static class IsNegative extends TypeSafeMatcher<Long> {

    @Override
    protected boolean matchesSafely(Long value) {
      return value < 0;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("a negative number");
    }
  }

  private static class IsPositive extends TypeSafeMatcher<Long> {

    @Override
    protected boolean matchesSafely(Long value) {
      return value > 0;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("a positive number");
    }
  }

  private static class IsZero extends TypeSafeMatcher<Long> {

    @Override
    protected boolean matchesSafely(Long value) {
      return value == 0;
    }

    @Override
    public void describeTo(Description description) {
      description.appendValue(0);
    }
  }

  public static Matcher<Long> isNegative() {
    return new IsNegative();
  }

  public static Matcher<Long> isPositive() {
    return new IsPositive();
  }

  public static Matcher<Long> isZero() {
    return new IsZero();
  }
}
