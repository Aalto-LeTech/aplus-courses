package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.PathUtil;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public abstract class CodeListener implements DocumentListener, ActivitiesListener {

  protected final ListenerCallback callback;
  protected final Project project;
  private Disposable disposable;
  protected final String filePath;
  protected final AtomicBoolean isCorrect = new AtomicBoolean(false);

  protected CodeListener(ListenerCallback callback, Project project, String filePath) {
    this.callback = callback;
    this.project = project;
    this.filePath = filePath;
  }

  @Override
  public boolean registerListener() {
    EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
    disposable = Disposer.newDisposable();
    multicaster.addDocumentListener(this, disposable);
    return checkFile();
  }

  @Override
  public void unregisterListener() {
    EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
    multicaster.removeDocumentListener(this);
    disposable.dispose();
  }

  private boolean checkFile() {
    if (project.getBasePath() != null) {
      Path modulePath = Path.of(project.getBasePath(), filePath);
      modulePath = Paths.get(PathUtil.toSystemDependentName(modulePath.toString()));
      VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(modulePath.toFile());
      if (vf != null) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(vf);
        if (psiFile != null) {
          checkPsiFile(psiFile);
        }
      }
    }
    return isCorrect.get();
  }

  @Override
  public void documentChanged(@NotNull DocumentEvent event) {
    PsiDocumentManager.getInstance(project).commitDocument(event.getDocument());
    PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(event.getDocument());
    if (psiFile != null) {
      //use thread pool in the future
      Thread checkPsiFileThread = new Thread(() ->
          ReadAction.run(() -> {
            if (psiFile.isValid()) {
              checkPsiFile(psiFile);
            }
          })
      );
      checkPsiFileThread.start();
    }
  }

  protected abstract void checkPsiFile(@NotNull PsiFile file);

}
