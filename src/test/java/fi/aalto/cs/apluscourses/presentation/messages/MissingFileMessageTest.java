package fi.aalto.cs.apluscourses.presentation.messages;

import static org.hamcrest.Matchers.containsString;

import fi.aalto.cs.apluscourses.presentation.messages.MissingFileMessage;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;

public class MissingFileMessageTest {

  @Test
  public void testMissingFileMessage() {
    Path path = Paths.get("awesome_module");
    String filename = "awesome file";
    MissingFileMessage message = new MissingFileMessage(path, filename);
    Assert.assertThat("The content should contain the path", message.getContent(),
        containsString("awesome_module"));
    Assert.assertThat("The content should contain the filename", message.getContent(),
        containsString("awesome file"));
    Assert.assertEquals(path, message.getPath());
    Assert.assertEquals(filename, message.getFilename());
  }

}
