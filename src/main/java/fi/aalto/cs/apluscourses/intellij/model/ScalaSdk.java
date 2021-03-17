package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import fi.aalto.cs.apluscourses.utils.content.Content;
import fi.aalto.cs.apluscourses.utils.content.RemoteContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.scala.project.ScalaLanguageLevel;
import org.jetbrains.plugins.scala.project.ScalaLibraryProperties;
import org.jetbrains.plugins.scala.project.ScalaLibraryPropertiesState;
import org.jetbrains.plugins.scala.project.ScalaLibraryType;

public class ScalaSdk extends IntelliJLibrary<PersistentLibraryKind<ScalaLibraryProperties>, ScalaLibraryPropertiesState> {

  /**
   * Constructs a new Scala SDK object.
   *
   * @param name    Name that must match scala-sdk-0.0.0 pattern.
   * @param project The IntelliJ project.
   */
  public ScalaSdk(@NotNull String name, @NotNull APlusProject project) {
    super(name, project);
  }

  @NotNull
  public String getScalaVersion() {
    return getName().replace("scala-sdk-", "");
  }

  @Override
  @NotNull
  protected Content getContent() {
    String version = getScalaVersion();
    return new RemoteContent.Zipped(
        "https://scala-lang.org/files/archive/scala-" + version + ".zip",
        "scala-" + version + "/lib/");
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
        ScalaLanguageLevel.findByVersion(getScalaVersion()).get(),
        getUris(getCompilerRoots()));
  }
}
