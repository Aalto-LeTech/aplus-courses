package fi.aalto.cs.apluscourses.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public class FileDoesNotExistException extends Exception {

  private static final long serialVersionUID = 526305689216094388L;

  @NotNull
  private transient Path path;
  @NotNull
  private final String name;

  public FileDoesNotExistException(@NotNull Path path,
                                   @NotNull String name) {
    this.path = path;
    this.name = name;
  }

  @NotNull
  public Path getPath() {
    return path;
  }

  @NotNull
  public String getName() {
    return name;
  }

  private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
    oos.writeObject(path.toString());
  }

  private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
    String pathString = (String) ois.readObject();
    this.path = Path.of(pathString);
  }
}
