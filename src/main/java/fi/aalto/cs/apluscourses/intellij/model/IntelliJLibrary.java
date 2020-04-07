package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import fi.aalto.cs.apluscourses.model.Library;
import java.nio.file.Paths;
import java.util.Objects;
import org.jetbrains.annotations.CalledWithWriteLock;
import org.jetbrains.annotations.NotNull;

public abstract class IntelliJLibrary<
    K extends PersistentLibraryKind<? extends LibraryProperties<S>>, S> extends Library {

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

  @CalledWithWriteLock
  private void loadInternal() {
    LibraryTable.ModifiableModel libraryTable = LibraryTablesRegistrar.getInstance()
        .getLibraryTable(getProject())
        .getModifiableModel();
    com.intellij.openapi.roots.libraries.Library.ModifiableModel library = libraryTable
        .createLibrary(getName(), getLibraryKind())
        .getModifiableModel();
    for (String uri : getUris()) {
      library.addRoot(uri, OrderRootType.CLASSES);
    }
    //HACK: this is the only way to access properties that I am aware of
    //noinspection unchecked
    initializeLibraryProperties(((LibraryEx) library).getProperties());
    library.commit();
    libraryTable.commit();
  }

  @Override
  public void load() {
    WriteAction.runAndWait(this::loadInternal);
  }

  public String getPath() {
    return Paths.get(getBasePath(), "lib", getName()).toString();
  }

  protected abstract String[] getUris();

  protected abstract K getLibraryKind();

  @CalledWithWriteLock
  protected abstract void initializeLibraryProperties(LibraryProperties<S> properties);
}
