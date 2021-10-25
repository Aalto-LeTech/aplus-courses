package fi.aalto.cs.apluscourses.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class FileDoesNotExistExceptionTest {

  @Test
  public void testCreate() {
    Path path = Paths.get("some", "path");
    String name = "stuff.txt";
    FileDoesNotExistException exception = new FileDoesNotExistException(path, name);
    assertEquals(path, exception.getPath());
    assertEquals(name, exception.getName());
  }
}
