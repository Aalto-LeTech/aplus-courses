package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.messages.MessageBusConnection;
import fi.aalto.cs.apluscourses.intellij.psi.ScalaFunctionDefinition;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
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
    BulkFileListener {

  private final ListenerCallback callback;
  private final Project project;
  private final String filePath;
  private MessageBusConnection messageBusConnection;
  private final ScalaFunctionDefinition scalaFunctionDefinition;
  private final AtomicBoolean isCorrect = new AtomicBoolean(false);

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
  public void after(@NotNull List<? extends VFileEvent> events) {
    events.forEach(event -> {
      if (event instanceof VFileContentChangeEvent
          && ((VFileContentChangeEvent) event).getFile().getPath().equals(
              project.getBasePath() + filePath)) {
        checkFile();
      }
    });
  }

  @Override
  public boolean registerListener() {
    messageBusConnection = project.getMessageBus().connect();
    messageBusConnection.subscribe(VirtualFileManager.VFS_CHANGES, this);
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
    if (messageBusConnection != null) {
      messageBusConnection.disconnect();
      messageBusConnection = null;
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
