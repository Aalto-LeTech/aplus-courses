package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.NonBlockingReadAction;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CodeListener extends ActivitiesListenerBase<@Nullable PsiFile> implements DocumentListener {

  protected final Project project;
  private final Disposable disposable = Disposer.newDisposable();
  protected final String filePath;

  protected CodeListener(ListenerCallback callback, Project project, String filePath) {
    super(callback);
    this.project = project;
    this.filePath = filePath;
  }

  @Override
  public void registerListenerOverride() {
    EditorFactory.getInstance().getEventMulticaster().addDocumentListener(this, disposable);
  }

  @Override
  public void unregisterListenerOverride() {
    EditorFactory.getInstance().getEventMulticaster().removeDocumentListener(this);
    disposable.dispose();
  }

  @Override
  public void documentChanged(@NotNull DocumentEvent event) {
    check(PsiDocumentManager.getInstance(project).getPsiFile(event.getDocument()));
  }

  @Override
  protected boolean checkOverride(@Nullable PsiFile param) {
    return param != null && checkPsiFile(param);
  }

  @Override
  protected <V> NonBlockingReadAction<V> prepareReadAction(@NotNull NonBlockingReadAction<V> action) {
    return action.withDocumentsCommitted(project);
  }

  @Override
  @RequiresReadLock
  protected @Nullable PsiFile getDefaultParameter() {
    String basePath = project.getBasePath();
    if (basePath == null) {
      return null;
    }
    Path modulePath = Path.of(basePath, filePath);
    VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(modulePath.toFile());
    if (vf == null) {
      return null;
    }
    return PsiManager.getInstance(project).findFile(vf);
  }

  protected abstract boolean checkPsiFile(@NotNull PsiFile psiFile);
}
