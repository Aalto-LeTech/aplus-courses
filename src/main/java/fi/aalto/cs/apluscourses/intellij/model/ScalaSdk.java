package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import fi.aalto.cs.apluscourses.utils.content.Content;
import fi.aalto.cs.apluscourses.utils.content.RemoteZippedDir;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.scala.project.ScalaLanguageLevel;
import org.jetbrains.plugins.scala.project.ScalaLibraryProperties;
import org.jetbrains.plugins.scala.project.ScalaLibraryPropertiesState;
import org.jetbrains.plugins.scala.project.ScalaLibraryType;

public class ScalaSdk extends IntelliJLibrary
    <PersistentLibraryKind<ScalaLibraryProperties>, ScalaLibraryPropertiesState> {

  @NotNull
  protected final String scalaVersion;

  /**
   * Constructs a new Scala SDK object.
   */
  public ScalaSdk(@NotNull String name,
                  @NotNull String scalaVersion,
                  @NotNull APlusProject project) {
    super(name, project);
    this.scalaVersion = scalaVersion;
  }

  @Override
  @NotNull
  protected Content @NotNull[] getContents() {
    return new Content[] {
        new RemoteZippedDir(
            "https://scala-lang.org/files/archive/scala-" + scalaVersion + ".zip",
            "scala-" + scalaVersion + "/lib")
    };
  }

  @Override
  @NotNull
  protected String @NotNull[] getClassRoots() {
    return new String[] {
      "scala-library.jar",
      "scala-reflect.jar"
    };
  }

  @NotNull
  protected String @NotNull[] getCompilerRoots() {
    return getJarFiles();
  }

  @Override
  @NotNull
  public PersistentLibraryKind<ScalaLibraryProperties> getLibraryKind() {
    return ScalaLibraryType.Kind$.MODULE$;
  }

  @Override
  @Nullable
  protected ScalaLibraryPropertiesState getPropertiesState(
      @Nullable ScalaLibraryPropertiesState currentState) {
    return new ScalaLibraryPropertiesState(
        ScalaLanguageLevel.findByVersion(scalaVersion).get(),
        getUris(getCompilerRoots()));
  }
}
