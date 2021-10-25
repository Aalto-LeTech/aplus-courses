package fi.aalto.cs.apluscourses.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import org.junit.jupiter.api.Test;

public class ComponentLoadExceptionTest {

  @Test
  public void testCreateModuleLoadException() {
    String componentName = "errorComponent";
    Throwable cause = new Throwable();
    ComponentLoadException exception = new ComponentLoadException(componentName, cause);
    assertEquals("The name of the component should be the same that was given to the constructor.",
        componentName, exception.getComponentName());
    assertSame("The cause should be the same that was given to the constructor.",
        cause, exception.getCause());
    assertThat("The message should contain the name of the component.",
        exception.getMessage(), containsString(componentName));
  }
}
