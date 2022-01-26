package fi.aalto.cs.apluscourses.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NoSuchComponentExceptionTest {

  @Test
  void testCreateNoSuchComponentException() {
    Throwable cause = new Throwable();
    String componentName = "Awesome component";
    NoSuchComponentException exception = new NoSuchComponentException(componentName, cause);
    Assertions.assertEquals(cause, exception.getCause(),
        "The cause of the exception should be the one given to the constructor");
    MatcherAssert.assertThat("The message should contain the name of the module", exception.getMessage(),
        containsString(componentName));
  }
}
