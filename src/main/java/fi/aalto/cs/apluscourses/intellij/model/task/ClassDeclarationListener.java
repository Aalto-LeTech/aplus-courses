package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaPsiElement;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaRecursiveElementVisitor;
import org.jetbrains.plugins.scala.lang.psi.api.base.ScPrimaryConstructor;
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScClass;
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.templates.ScExtendsBlockImpl;
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.templates.ScTemplateParentsImpl;

public class ClassDeclarationListener implements ActivitiesListener, DocumentListener {

  private final ListenerCallback callback;
  private final Project project;
  private final String className;
  private String[] arguments;
  private String[] hierarchy;
  private Disposable disposable;
  private PsiFile psiFile;
  private boolean complete = true;

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
                                  String[] arguments,
                                  String[] hierarchy,
                                  String fileName) {
    this.callback = callback;
    this.project = project;
    this.className = className;
    this.arguments = arguments;
    this.hierarchy = hierarchy;
    Path modulePath = Paths.get(project.getBasePath() + fileName);
    VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(modulePath.toFile());
    psiFile = PsiManager.getInstance(project).findFile(vf);
    checkPsiFile(psiFile);
    //TODO check if the conditinos are already met!
  }

  @Override
  public boolean registerListener() {
    EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
    disposable = Disposer.newDisposable();
    multicaster.addDocumentListener(this, disposable);
    //make sure the correct class file is open?
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
    PsiDocumentManager.getInstance(project).commitDocument(event.getDocument());
    complete = true;
    PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(event.getDocument());
    checkPsiFile(psiFile);
  }

  private void checkPsiFile(PsiFile file) {
    psiFile.accept(new ScalaRecursiveElementVisitor() {
      @Override
      public void visitScalaElement(ScalaPsiElement element) {
        super.visitScalaElement(element);
        checkPsiElement(element);
      }
    });
    if (complete) {
      ApplicationManager.getApplication().invokeLater(callback::callback);
    }
  }

  private boolean checkScClass(ScClass element) {
    return className.equals(element.getName());
  }

  private boolean checkScPrimaryConstructor(ScPrimaryConstructor element) {
    Map<String, String> args = new HashMap<>();
    for (String a: arguments) {
      StringTokenizer tokenizer = new StringTokenizer(a, ":", false);
      String key = tokenizer.nextToken();
      String value = tokenizer.nextToken();
      args.put(key, value);
    }
    boolean correct = true;
    PsiParameter[] parameters = element.parameterList().getParameters();
    for (PsiParameter param: parameters) {
      if (!args.containsKey(param.getName())
          || !args.get(param.getName()).equals(param.getType().getPresentableText())) {
        correct = false;
      }
    }
    return correct;
  }

  private boolean checkExtendsBlock(ScExtendsBlockImpl element) {
    List<String> hierarchies = new ArrayList<>();
    PsiElement[] children = element.getChildren();
    Optional<PsiElement> parent = Arrays.stream(children).filter(
        child -> child instanceof ScTemplateParentsImpl).findFirst();
    if (parent.isPresent()) {
      children = parent.get().getChildren();
      Arrays.stream(children).forEach(child -> hierarchies.add(child.getText()));
      return hierarchies.equals(Arrays.asList(hierarchy));
    }
    return false;
  }

  private boolean checkPsiElement(ScalaPsiElement element) {
    if ((element instanceof ScClass && !checkScClass((ScClass) element))
        || (element instanceof ScPrimaryConstructor
            && !checkScPrimaryConstructor((ScPrimaryConstructor) element))
        || (element instanceof ScExtendsBlockImpl
        && !checkExtendsBlock((ScExtendsBlockImpl) element))) {
      complete = false;
    }
    return complete;
  }
}
