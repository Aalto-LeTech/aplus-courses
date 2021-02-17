package fi.aalto.cs.apluscourses.model;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

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
