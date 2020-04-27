package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import fi.aalto.cs.apluscourses.model.Library;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.annotations.CalledWithWriteLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class IntelliJLibrary
    <K extends PersistentLibraryKind<? extends LibraryProperties<S>>, S>
    extends Library
    implements IntelliJComponent<com.intellij.openapi.roots.libraries.Library> {

  @NotNull
  protected final APlusProject project;

  public IntelliJLibrary(@NotNull String name, @NotNull APlusProject project, int state) {
    super(name, state);
    this.project = project;
  }

  /**
   * Method that adds libraries to SDK root.
   */
  @CalledWithWriteLock
  public void loadInternal() {
    LibraryTable.ModifiableModel libraryTable = project.getLibraryTable().getModifiableModel();
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

  @NotNull
  @Override
  public Path getPath() {
    return Paths.get("lib", getName());
  }

  @NotNull
  public Path getFullPath() {
    return project.getBasePath().resolve(getPath());
  }

  @Override
  protected int resolveStateInternal() {
    return project.resolveComponentState(this);
  }

  protected abstract String[] getUris();

  protected abstract K getLibraryKind();

  @CalledWithWriteLock
  protected abstract void initializeLibraryProperties(LibraryProperties<S> properties);

  @Override
  @Nullable
  public com.intellij.openapi.roots.libraries.Library getPlatformObject() {
    return project.getLibraryTable().getLibraryByName(getName());
  }
}
