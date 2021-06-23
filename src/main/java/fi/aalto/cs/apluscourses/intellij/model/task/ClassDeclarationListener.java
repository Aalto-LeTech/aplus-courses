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
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import fi.aalto.cs.apluscourses.intellij.psi.ScalaClassDeclaration;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaPsiElement;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaRecursiveElementVisitor;
import org.jetbrains.plugins.scala.lang.psi.api.base.ScPrimaryConstructor;
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScTypeParamClause;
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScClass;
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.templates.ScExtendsBlockImpl;

public class ClassDeclarationListener implements ActivitiesListener, DocumentListener {

  private final ListenerCallback callback;
  private final Project project;
  private final String fileName;
  private Disposable disposable;
  private final ScalaClassDeclaration modelScalaClass;
  private final AtomicBoolean isCorrect = new AtomicBoolean(false);


  /**
   * Constructor.
   */
  public ClassDeclarationListener(ListenerCallback callback,
                                  Project project,
                                  String className,
                                  String[] arguments,
                                  String[] hierarchy,
                                  String[] typeParameters,
                                  String[] parameterModifiers,
                                  String[] parameterAnnotations,
                                  String fileName) {
    this.callback = callback;
    this.project = project;
    this.modelScalaClass = new ScalaClassDeclaration(className,
        arguments, hierarchy, typeParameters, parameterModifiers, parameterAnnotations);
    this.fileName = fileName;
  }

  /**
   * Checks the file when the listener is first registered.
   */
  public boolean checkFile() {
    Path modulePath = Paths.get(project.getBasePath() + fileName);
    VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(modulePath.toFile());
    if (vf != null) {
      checkPsiFile(PsiManager.getInstance(project).findFile(vf));
    }
    return isCorrect.get();
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

  @Override
  public void documentChanged(@NotNull DocumentEvent event) {
    PsiDocumentManager.getInstance(project).commitDocument(event.getDocument());
    PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(event.getDocument());
    checkPsiFile(psiFile);
  }

  private void checkPsiFile(@Nullable PsiFile psiFile) {
    if (psiFile == null) {
      return;
    }
    psiFile.accept(new ScalaRecursiveElementVisitor() {
      @Override
      public void visitScalaElement(ScalaPsiElement element) {
        super.visitScalaElement(element);
        if (element instanceof ScClass
            && modelScalaClass.checkClassName(((ScClass) element).getName())) {
          PsiElement[] children = element.getChildren();
          Optional<PsiElement> typeParameters = Arrays.stream(children).filter(
              ScTypeParamClause.class::isInstance).findFirst();
          Optional<PsiElement> constructor = Arrays.stream(children).filter(
              ScPrimaryConstructor.class::isInstance).findFirst();
          if (modelScalaClass.checkConstructor(constructor)
              && modelScalaClass.checkTypeParameters(typeParameters)) {
            Optional<PsiElement> extendsBlock = Arrays.stream(children).filter(
                ScExtendsBlockImpl.class::isInstance).findFirst();
            if (modelScalaClass.checkExtendsBlock(extendsBlock)) {
              ApplicationManager.getApplication().invokeLater(callback::callback);
              isCorrect.set(true);
            }
          }
        }
      }
    });
  }

}
