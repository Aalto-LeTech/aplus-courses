package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.roots.impl.libraries.UnknownLibraryKind;
import fi.aalto.cs.apluscourses.utils.content.Content;
import fi.aalto.cs.apluscourses.utils.content.RemoteZippedFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class ScalaLibrary extends IntelliJLibrary<UnknownLibraryKind, Element> {

  private final String scalaVersion;

  protected ScalaLibrary(@NotNull String name,
                         @NotNull String scalaVersion,
                         @NotNull APlusProject project) {
    super(name, project);
    this.scalaVersion = scalaVersion;
  }

  @Override
  @NotNull
  protected Content @NotNull [] getContents() {
    return new Content[] {
        new RemoteZippedFile(
            "https://scala-lang.org/files/archive/scala-" + scalaVersion + ".zip",
            "scala-" + scalaVersion + "/lib/scala-library.jar")
    };
  }
}
