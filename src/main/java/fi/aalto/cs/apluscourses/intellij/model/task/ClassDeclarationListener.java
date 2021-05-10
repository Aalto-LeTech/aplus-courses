package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class ClassDeclarationListener implements ActivitiesListener, DocumentListener {

  private final ListenerCallback callback;
  private final Project project;
  private final String className;
  private String arguments;
  private Disposable disposable;

  /**
   * Constructor.
   * @param callback the callback for when the task is complete
   * @param project The project where the Tutorial is happening
   * @param className The class name
   * @param arguments The arguments of the class
   */
  public ClassDeclarationListener(ListenerCallback callback,
                                  Project project,
                                  String className,
                                  String arguments) { //also get the arguments for the class
    //regex might fit better here!
    this.callback = callback;
    this.project = project;
    this.className = className;
    this.arguments = arguments;
  }

  @Override
  public boolean registerListener() {
    EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
    disposable = Disposer.newDisposable();
    multicaster.addDocumentListener(this, disposable);
    //make sure the correct class file is open
    return false;
  }

  @Override
  public void unregisterListener() {
    EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
    multicaster.removeDocumentListener(this);
    disposable.dispose();
  }

  @Override
  public void documentChanged(@NotNull DocumentEvent event) {
    PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(event.getDocument());
    LeafPsiElement element = (LeafPsiElement) psiFile.findElementAt(event.getOffset());
    try {
      if (checkFile(psiFile)) {
        callback.callback();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String getFileText(PsiFile psiFile) throws IOException {
    String fileText = VfsUtil.loadText(psiFile.getVirtualFile());
    arguments = arguments.replace(" ", "").replace("\n", "").replace("\t", "");
    return fileText.replace(" ", "").replace("\n", "").replace("\t", "");
  }

  private boolean checkFile(PsiFile psiFile) throws IOException {
    String fileText = getFileText(psiFile);
    /*Pattern classDeclarationPattern = Pattern.compile("class" +
    className + "\\(" + arguments + "\\)\\{");
    Matcher matcher = classDeclarationPattern.matcher(fileText);
    return matcher.matches();*/
    return fileText.contains("class" + className + "(" + arguments + "){");
  }
}
