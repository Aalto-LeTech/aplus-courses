package fi.aalto.cs.apluscourses.presentation.messages;

import static org.hamcrest.Matchers.containsString;

import org.junit.Assert;
import org.junit.Test;

public class MissingModuleMessageTest {

  @Test
  public void testMissingModuleMessage() {
    String name = "module name";
    MissingModuleMessage message = new MissingModuleMessage(name);
    Assert.assertEquals("Module name should be correct", name, message.getModuleName());
    Assert.assertThat("The content should contain the module name", message.getContent(),
        containsString(name));
    Assert.assertThat("The title should contain word 'module'", message.getContent(),
        containsString("module"));
  }

}
