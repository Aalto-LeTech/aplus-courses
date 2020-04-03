package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import fi.aalto.cs.apluscourses.model.Library;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.project.ScalaLibraryProperties;
import org.jetbrains.plugins.scala.project.ScalaLibraryType$;

public abstract class IntelliJLibrary extends Library {

  @NotNull
  protected final Project project;

  public IntelliJLibrary(@NotNull String name, @NotNull Project project) {
    super(name);
    this.project = project;
  }

  @NotNull
  protected String getBasePath() {
    return Objects.requireNonNull(getProject().getBasePath());
  }

  @NotNull
  public Project getProject() {
    return project;
  }

  private void loadInternal() {
    com.intellij.openapi.roots.libraries.Library.ModifiableModel modifiableModel =
        LibraryTablesRegistrar.getInstance()
            .getLibraryTable(getProject())
            .createLibrary(getName())
            .getModifiableModel();
    for (String uri : getUris()) {
      modifiableModel.addRoot(uri, OrderRootType.CLASSES);
    }
    modifiableModel.commit();
  }

  @Override
  public void load() {
    WriteAction.runAndWait(this::loadInternal);
  }

  protected abstract String[] getUris();
}
