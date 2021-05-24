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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaPsiElement;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaRecursiveElementVisitor;
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunctionDefinition;
import org.jetbrains.plugins.scala.lang.psi.impl.expr.ScReferenceExpressionImpl;
import org.jetbrains.plugins.scala.lang.psi.impl.statements.params.ScParametersImpl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FunctionDefinitionListener implements ActivitiesListener, DocumentListener {

  private final ListenerCallback callback;
  private final Project project;
  private final String methodName;
  private Disposable disposable;
  private final String[] arguments;
  private final String[] methodBody;
  private final String returnType;

  public FunctionDefinitionListener(ListenerCallback callback,
                                    Project project, String methodName, String[] arguments, String[] body, String returnType, String filePath) {
    this.callback = callback;
    this.project = project;
    this.methodName = methodName;
    this.arguments = arguments;
    this.methodBody = body;
    this.returnType = returnType;
    Path modulePath = Paths.get(project.getBasePath() + filePath);
    VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(modulePath.toFile());
    PsiFile psiFile = PsiManager.getInstance(project).findFile(vf);
    checkPsiFile(psiFile);
  }

  @Override
  public boolean registerListener() {
    EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
    disposable = Disposer.newDisposable();
    multicaster.addDocumentListener(this, disposable);
    //and then check if the method is already in the desired condition
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
    PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(event.getDocument());
    psiFile.clearCaches();
    checkPsiFile(psiFile);
  }

  private void checkPsiFile(PsiFile psiFile) {
    psiFile.accept(new ScalaRecursiveElementVisitor() {
      @Override
      public void visitScalaElement(ScalaPsiElement element) {
        super.visitScalaElement(element);
        if (element instanceof ScFunctionDefinition
                && methodName.equals(((ScFunctionDefinition) element).getName())) {
          System.out.println(((ScFunctionDefinition) element).getReturnType().getPresentableText());
          if (returnType.equals(((ScFunctionDefinition) element).getReturnType().getPresentableText())) {
            PsiElement[] children = element.getChildren();
            Optional<PsiElement> opt = Arrays.stream(children).filter(
                c -> c instanceof ScParametersImpl).findFirst();
            if (opt.isPresent() && checkParameters(((ScParametersImpl) opt.get()).getParameters())) {
              opt = Optional.ofNullable(children[children.length - 1]);
              if (opt.isPresent()) {
                children = opt.get().getChildren();
                if (checkMethodBody(children)) {
                  ApplicationManager.getApplication().invokeLater(callback::callback);
                }
              }
            }
          }
        }
      }
    });
  }

  private boolean checkParameters(PsiParameter[] parameters) {
    Map<String, String> args = new HashMap<>();
    for (String a: arguments) {
      StringTokenizer tokenizer = new StringTokenizer(a, ":", false);
      String key = tokenizer.nextToken();
      String value = tokenizer.nextToken();
      args.put(key, value);
    }
    boolean complete = true;
    for (PsiParameter param: parameters) {
      if (!args.containsKey(param.getName())
          || !args.get(param.getName()).equals(param.getType().getPresentableText())) {
        complete = false;
      }
    }

    return complete && parameters.length == args.size();
  }

  private boolean checkMethodBody(PsiElement[] method) {
    Collection<PsiElement> totalElements = getMethodBodyPsiElements(method);
    List<String> args = Arrays.asList(methodBody.clone());
    List<String> body = new ArrayList<>();
    totalElements.forEach(element -> body.add(element.getText()));
    return args.equals(body);
  }

  private Collection<PsiElement> getMethodBodyPsiElements(PsiElement[] methodElements) {
    List<PsiElement> elements = new ArrayList<>();
    for (PsiElement element: methodElements) {
      if (element instanceof PsiWhiteSpace) {
        continue;
      }
      if (element.getChildren().length == 0 || element instanceof ScReferenceExpressionImpl) {
        elements.add(element);
      } else {
        elements.addAll(getMethodBodyPsiElements(element.getChildren()));
      }
    }
    return elements;
  }
}
