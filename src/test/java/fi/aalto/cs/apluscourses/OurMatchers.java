package fi.aalto.cs.apluscourses;

import java.util.Objects;
import java.util.Optional;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class OurMatchers {

  private static class IsEmpty extends TypeSafeMatcher<Optional<?>> {
    @Override
    protected boolean matchesSafely(Optional<?> o) {
      return o.isEmpty();
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("empty");
    }
  }

  private static class HasValue extends TypeSafeMatcher<Optional<?>> {
    private final Object expected;

    public HasValue(Object expected) {
      this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(Optional<?> value) {
      return value.isPresent() && Objects.equals(expected, value.get());
    }

    @Override
    public void describeTo(Description description) {
      description.appendValue(expected);
    }
  }

  public static Matcher<Optional<?>> isEmpty() {
    return new IsEmpty();
  }

  public static <T> Matcher<Optional<?>> hasValue(Object expected) {
    return new HasValue(expected);
  }
}
