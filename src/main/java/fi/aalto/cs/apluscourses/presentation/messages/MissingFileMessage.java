package fi.aalto.cs.apluscourses.presentation.messages;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public class MissingFileMessage implements Message {
  @NotNull
  private final Path path;

  @NotNull
  private final String filename;

  public MissingFileMessage(@NotNull Path path, @NotNull String filename) {
    this.path = path;
    this.filename = filename;
  }

  @NotNull
  public Path getPath() {
    return path;
  }

  @NotNull
  public String getFilename() {
    return filename;
  }

  @NotNull
  @Override
  public String getContent() {
    return "A+ Courses plugin couldn't find the file " + filename + " in directory " + path
        + ". Please double-check from which module you intend to submit.";
  }

  @NotNull
  @Override
  public String getTitle() {
    return  "Could not find file";
  }

  @NotNull
  @Override
  public Level getLevel() {
    return Level.ERR;
  }
}
