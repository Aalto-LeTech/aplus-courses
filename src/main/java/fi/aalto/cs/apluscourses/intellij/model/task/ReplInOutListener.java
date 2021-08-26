package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.SyntaxTraverser;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.ScalaLanguage;

public class ReplInOutListener extends ScalaReplListener {
  @NotNull
  private final String output;

  private List<PsiElement> correctElements;

  /**
   * A constructor with the wanted input and output as a string.
   */
  public ReplInOutListener(@NotNull ListenerCallback callback,
                           @NotNull Project project,
                           @NotNull String input,
                           @NotNull String output,
                           @NotNull String module) {
    super(callback, project, module);
    ApplicationManager.getApplication().runReadAction(() -> {
      var inputFile = PsiFileFactory.getInstance(project).createFileFromText(ScalaLanguage.INSTANCE, input);
      correctElements = SyntaxTraverser
          .psiTraverser(inputFile)
          .traverse()
          .filter(element -> !(element instanceof PsiWhiteSpace) && element instanceof LeafPsiElement)
          .toList();
    });
    this.output = output;
  }

  /**
   * Creates an instance of SingleLineReplListener based on the provided arguments.
   */
  public static ReplInOutListener create(ListenerCallback callback,
                                         Project project, Arguments arguments) {
    return new ReplInOutListener(callback, project,
        arguments.getString("input"),
        arguments.getString("output"),
        arguments.getString("module"));
  }

  @Override
  protected boolean isCorrect(@NotNull ProcessEvent event) {
    if (file == null || correctElements == null) {
      return false;
    }

    final var inputElements = SyntaxTraverser
        .psiTraverser(file)
        .traverse()
        .filter(element -> !(element instanceof PsiWhiteSpace) && element instanceof LeafPsiElement)
        .toList();

    if (inputElements.size() != correctElements.size()) {
      return false;
    }


    return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> {
      for (int i = 0; i < correctElements.size(); i++) {
        if (!inputElements.get(i).getText().equals(correctElements.get(i).getText())) {
          return false;
        }
      }

      return (output + "\n").equals(getEventText(event));
    });
  }
}
