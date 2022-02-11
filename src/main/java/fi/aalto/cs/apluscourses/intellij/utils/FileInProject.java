package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import fi.aalto.cs.apluscourses.utils.Lazy;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.annotations.NotNull;

public class FileInProject {

  private final @NotNull Project project;
  private final @NotNull String relativePath;
  private final Lazy<Path> lazyAbsolutePath = Lazy.of(this::getAbsolutePathInternal);
  private final Lazy<VirtualFile> lazyVirtualFile = Lazy.of(this::getVirtualFileInternal);
  private final Lazy<Document> lazyDocument = Lazy.of(this::getDocumentInternal);
  private final Lazy<PsiFile> lazyPsiFile = Lazy.of(this::getPsiFileInternal);

  public FileInProject(@NotNull Project project, @NotNull String relativePath) {
    this.project = project;
    this.relativePath = relativePath;
  }

  public @NotNull Path getAbsolutePath() {
    return lazyAbsolutePath.get();
  }

  public @NotNull VirtualFile getVirtualFile() {
    return lazyVirtualFile.get();
  }

  public @NotNull Document getDocument() {
    return lazyDocument.get();
  }

  public @NotNull PsiFile getPsiFile() {
    return lazyPsiFile.get();
  }

  private @NotNull Path getAbsolutePathInternal() {
    var basePath = project.getBasePath();
    if (basePath == null) {
      throw new IllegalArgumentException("Project's base path is not defined.");
    }
    return Paths.get(basePath, relativePath);
  }

  private @NotNull VirtualFile getVirtualFileInternal() {
    var path = getAbsolutePath();
    var vf = VirtualFileManager.getInstance().findFileByNioPath(path);
    if (vf == null) {
      throw new IllegalArgumentException("File '" + relativePath + "' was not found!");
    }
    return vf;
  }

  private @NotNull Document getDocumentInternal() {
    var vf = getVirtualFile();
    var document = FileDocumentManager.getInstance().getDocument(vf);
    if (document == null) {
      throw new IllegalArgumentException("File '" + relativePath + "' is not valid.");
    }
    return document;
  }

  private @NotNull PsiFile getPsiFileInternal() {
    var document = getDocument();
    var psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
    if (psiFile == null) {
      throw new IllegalArgumentException("Could not get PSI file.");
    }
    return psiFile;
  }
}
