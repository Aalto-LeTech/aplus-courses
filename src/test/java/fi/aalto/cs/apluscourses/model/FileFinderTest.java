package fi.aalto.cs.apluscourses.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class FileFinderTest {

  @Test
  public void findFile() throws FileDoesNotExistException {
    Path dir = Paths.get("some", "dir");
    String name = "file.txt";
    FileFinder fileFinder = Path::resolve; // Path::resolve is an implementation of tryFindFile

    // asserts that findFile() delegates to tryFindFile()
    assertEquals(dir.resolve(name), fileFinder.findFile(dir, name));
  }

  @Test
  public void findFileThrows() {
    Path dir = Paths.get("data");
    String name = "ok.txt";
    FileFinder fileFinder = (what, ever) -> null;
    try {
      fileFinder.findFile(dir, name);
      fail();
    } catch (FileDoesNotExistException e) {
      assertEquals(name, e.getName());
      assertEquals(dir, e.getPath());
    }
  }

  @Test
  public void findFiles() throws FileDoesNotExistException {
    Path dir = Paths.get("root");
    String file0 = "foo";
    String file1 = "bar";

    FileFinder fileFinder = Path::resolve;
    Path[] paths = fileFinder.findFiles(dir, new String[] {file0, file1});

    assertEquals(2, paths.length);
    assertEquals(dir.resolve(file0), paths[0]);
    assertEquals(dir.resolve(file1), paths[1]);
  }
}
