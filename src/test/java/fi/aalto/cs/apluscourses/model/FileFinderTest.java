package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FileFinderTest {

  @Test
  void findFile() throws FileDoesNotExistException {
    Path dir = Paths.get("some", "dir");
    String name = "file.txt";
    FileFinder fileFinder = Path::resolve; // Path::resolve is an implementation of tryFindFile

    // asserts that findFile() delegates to tryFindFile()
    Assertions.assertEquals(dir.resolve(name), fileFinder.findFile(dir, name));
  }

  @Test
  void findFileThrows() {
    Path dir = Paths.get("data");
    String name = "ok.txt";
    FileFinder fileFinder = (what, ever) -> null;
    try {
      fileFinder.findFile(dir, name);
      Assertions.fail();
    } catch (FileDoesNotExistException e) {
      Assertions.assertEquals(name, e.getName());
      Assertions.assertEquals(dir, e.getPath());
    }
  }

  @Test
  void findFiles() throws FileDoesNotExistException {
    Path dir = Paths.get("root");
    String file0 = "foo";
    String file1 = "bar";

    FileFinder fileFinder = Path::resolve;
    Path[] paths = fileFinder.findFiles(dir, new String[] {file0, file1});

    Assertions.assertEquals(2, paths.length);
    Assertions.assertEquals(dir.resolve(file0), paths[0]);
    Assertions.assertEquals(dir.resolve(file1), paths[1]);
  }
}
