package fi.aalto.cs.apluscourses.intellij.model;

import fi.aalto.cs.apluscourses.utils.content.Content;
import fi.aalto.cs.apluscourses.utils.content.RemoteZippedDir;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public class Scala3Sdk extends ScalaSdk {

  /**
   * Constructs a new Scala3 SDK object.
   */
  public Scala3Sdk(@NotNull String name,
                   @NotNull String scalaVersion,
                   @NotNull APlusProject project) {
    super(name, scalaVersion, project);
  }

  @Override
  @NotNull
  protected Content @NotNull [] getContents() {
    return new Content[] {
        new RemoteZippedDir(
            "https://github.com/lampepfl/dotty/releases/download/"
                + scalaVersion + "/scala3-" + scalaVersion + ".zip",
            "scala3-" + scalaVersion + "/lib")
    };
  }

  @Override
  @NotNull
  protected String @NotNull [] getClassRoots() {
    return Arrays.stream(getJarFiles())
        .filter(lib -> lib.startsWith("scala-library") || lib.startsWith("scala3-library"))
        .toArray(String[]::new);
  }
}
