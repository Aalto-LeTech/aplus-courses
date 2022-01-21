package fi.aalto.cs.apluscourses.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ComponentLoadExceptionTest {

  @Test
  void testCreateModuleLoadException() {
    String componentName = "errorComponent";
    Throwable cause = new Throwable();
    ComponentLoadException exception = new ComponentLoadException(componentName, cause);
    Assertions.assertEquals(componentName, exception.getComponentName(),
        "The name of the component should be the same that was given to the constructor.");
    Assertions.assertSame(cause, exception.getCause(),
        "The cause should be the same that was given to the constructor.");
    MatcherAssert.assertThat("The message should contain the name of the component.", exception.getMessage(),
        containsString(componentName));
  }
}
