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
import fi.aalto.cs.apluscourses.intellij.psi.ScalaFunctionDefinition;
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
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunctionDefinition;
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScTypeParamClause;
import org.jetbrains.plugins.scala.lang.psi.impl.statements.params.ScParametersImpl;


public class FunctionDefinitionListener implements ActivitiesListener,
        DocumentListener {

  private final ListenerCallback callback;
  private final Project project;
  private final String filePath;
  private Disposable disposable;
  private ScalaFunctionDefinition scalaFunctionDefinition;
  private AtomicBoolean isCorrect = new AtomicBoolean(false);

  /**
   * Constructor.
   */
  public FunctionDefinitionListener(ListenerCallback callback,
                                    Project project, String methodName, String[] arguments,
                                    String[] body, String[] typeParameters, String filePath) {
    this.callback = callback;
    this.project = project;
    this.scalaFunctionDefinition = new ScalaFunctionDefinition(methodName,
        arguments, body, typeParameters);
    this.filePath = filePath;
  }

  @Override
  public boolean registerListener() {
    EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
    disposable = Disposer.newDisposable();
    multicaster.addDocumentListener(this, disposable);
    return checkFile();
  }

  private boolean checkFile() {
    Path modulePath = Paths.get(project.getBasePath() + filePath);
    VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(modulePath.toFile());
    if (vf != null) {
      checkPsiFile(PsiManager.getInstance(project).findFile(vf));
    }
    return isCorrect.get();
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
    if (psiFile != null) {
      checkPsiFile(psiFile);
    }
  }

  private void checkPsiFile(@Nullable PsiFile psiFile) {
    if (psiFile == null) {
      return;
    }
    psiFile.accept(new ScalaRecursiveElementVisitor() {
      @Override
      public void visitScalaElement(ScalaPsiElement element) {
        super.visitScalaElement(element);
        if (element instanceof ScFunctionDefinition
                && scalaFunctionDefinition.checkMethodName((ScFunctionDefinition) element)) {
          PsiElement[] children = element.getChildren();
          Optional<PsiElement> optTypeParameters = Arrays.stream(children).filter(
              ScTypeParamClause.class::isInstance).findFirst();
          Optional<PsiElement> optParameters = Arrays.stream(children).filter(
              ScParametersImpl.class::isInstance).findFirst();
          if (scalaFunctionDefinition.checkScTypeParametersClause(optTypeParameters)
               && scalaFunctionDefinition.checkParameters(optParameters)
                && scalaFunctionDefinition.checkFunctionBody(children)) {
            ApplicationManager.getApplication().invokeLater(callback::callback);
            isCorrect.set(true);
          }
        }
      }
    });
  }

}
