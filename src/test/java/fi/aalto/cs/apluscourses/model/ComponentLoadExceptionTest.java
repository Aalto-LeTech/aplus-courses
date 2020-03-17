package fi.aalto.cs.apluscourses.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ComponentLoadExceptionTest {

  @Test
  public void testCreateModuleLoadException() {
    String moduleName = "errorModule";
    Throwable cause = new Throwable();
    Module module = new ModelExtensions.TestModule(moduleName);
    ComponentLoadException exception = new ComponentLoadException(module, cause);
    assertSame("The module should be the same that was given to the constructor.",
        module, exception.getComponent());
    assertSame("The cause should be the same that was given to the constructor.",
        cause, exception.getCause());
    assertThat("The message should contain the name of the module.",
        exception.getMessage(), containsString(moduleName));
  }
}
