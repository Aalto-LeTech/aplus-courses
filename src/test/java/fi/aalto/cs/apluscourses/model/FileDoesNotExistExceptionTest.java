package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FileDoesNotExistExceptionTest {

  @Test
  void testCreate() {
    Path path = Paths.get("some", "path");
    String name = "stuff.txt";
    FileDoesNotExistException exception = new FileDoesNotExistException(path, name);
    Assertions.assertEquals(path, exception.getPath());
    Assertions.assertEquals(name, exception.getName());
  }
}
