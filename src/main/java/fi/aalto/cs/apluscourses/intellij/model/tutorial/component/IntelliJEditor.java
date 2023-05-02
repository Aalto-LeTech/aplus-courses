package fi.aalto.cs.apluscourses.intellij.model.tutorial.component;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import fi.aalto.cs.apluscourses.model.tutorial.CodeContext;
import fi.aalto.cs.apluscourses.model.tutorial.LineRange;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import java.awt.Component;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJEditor extends IntelliJTutorialComponent<Component> {

  private final @Nullable Path path;

  public IntelliJEditor(@Nullable Path path, @Nullable TutorialComponent parent, @Nullable Project project) {
    super(parent, project);
    var basePath = Optional.ofNullable(project).map(Project::getBasePath).map(Path::of).orElse(Path.of(""));
    this.path = Optional.ofNullable(path).map(basePath::resolve).orElse(null);
  }

  protected @Nullable VirtualFile getVirtualFile() {
    if (path == null) {
      return null;
    }
    var vf = VfsUtil.findFile(path, false);
    return vf != null && vf.isValid() ? vf : null;
  }

  private @NotNull Stream<@NotNull Editor> allEditors() {
    return Arrays.stream(EditorFactory.getInstance().getAllEditors());
  }

  private @NotNull Stream<@NotNull Editor> editors(@NotNull Document document) {
    return EditorFactory.getInstance().editors(document, getProject());
  }

  private @NotNull Stream<@NotNull Editor> editors() {
    return Optional.ofNullable(getVirtualFile())
        .map(FileDocumentManager.getInstance()::getCachedDocument)
        .map(this::editors)
        .orElseGet(this::allEditors);
  }

  public @Nullable Editor getEditor() {
    return editors().findFirst().orElse(null);
  }

  @Override
  public @Nullable Document getDocument() {
    return Optional.ofNullable(getEditor()).map(Editor::getDocument).orElse(null);
  }

  @Override
  public @Nullable Component getAwtComponent() {
    return Optional.ofNullable(getEditor())
        .map(Editor::getComponent)
        .orElse(null);
  }

  @Override
  public @NotNull CodeContext getCodeContext() {
    return new EditorCodeContext(null);
  }

  public class EditorCodeContext implements CodeContext {

    private final @Nullable LineRange lineRange;

    public EditorCodeContext(@Nullable LineRange lineRange) {
      this.lineRange = lineRange;
    }

    @Override
    public @Nullable Path getPath() {
      return Optional.ofNullable(getVirtualFile()).map(VirtualFile::toNioPath).orElse(null);
    }

    @Override
    public int getStartOffset() {
      var document = getDocument();
      if (document == null) {
        return 0;
      }
      int line = Optional.ofNullable(lineRange).map(LineRange::getFirst).orElse(1) - 1;
      return document.getLineStartOffset(line);
    }

    @Override
    public int getEndOffset() {
      var document = getDocument();
      if (document == null) {
        return 0;
      }
      int line = Optional.ofNullable(lineRange).map(LineRange::getLast).orElse(document.getLineCount()) - 1;
      return document.getLineEndOffset(line);
    }
  }
}
