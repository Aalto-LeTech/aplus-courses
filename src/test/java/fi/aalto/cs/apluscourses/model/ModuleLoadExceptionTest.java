package fi.aalto.cs.apluscourses.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Test;

public class ModuleLoadExceptionTest {

  @Test
  public void testCreateModuleLoadException() throws MalformedURLException {
    String moduleName = "errorModule";
    Throwable cause = new Throwable();
    Module module = new Module(moduleName, new URL("https://example.com"));
    ModuleLoadException exception = new ModuleLoadException(module, cause);
    assertSame("The module should be the same that was given to the constructor.",
        module, exception.getModule());
    assertSame("The cause should be the same that was given to the constructor.",
        cause, exception.getCause());
    assertThat("The message should contain the name of the module.",
        exception.getMessage(), containsString(moduleName));
  }
}
