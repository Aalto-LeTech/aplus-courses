package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.openapi.application.NonBlockingReadAction;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiCodeFragment;
import com.intellij.util.DocumentUtil;
import fi.aalto.cs.apluscourses.intellij.utils.FileInProject;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import org.jetbrains.annotations.NotNull;

public abstract class LineListener extends ActivitiesListenerBase<Void> implements DocumentListener {

  protected final Project project;
  protected final FileInProject file;
  protected final int line;
  protected final String expected;

  protected LineListener(@NotNull ListenerCallback callback,
                         @NotNull Project project,
                         @NotNull String filePath,
                         int lineOneBased,
                         @NotNull String expected) {
    super(callback);
    this.project = project;
    this.file = new FileInProject(project, filePath);
    this.line = lineOneBased - 1;
    this.expected = expected;
  }

  @Override
  public void registerListenerOverride() {
    file.getDocument().addDocumentListener(this, project);
  }

  @Override
  public void unregisterListenerOverride() {
    file.getDocument().removeDocumentListener(this);
  }

  @Override
  public void documentChanged(@NotNull DocumentEvent event) {
    if (event.getDocument().getLineNumber(event.getOffset()) == line) {
      check(null);
    }
  }

  @Override
  protected boolean checkOverride(Void nil) {
    var document = file.getDocument();
    var range = DocumentUtil.getLineTextRange(document, line);
    var actual = document.getText(range);
    return areCodeFragmentsEqual(expected, actual, this::getPsiCodeFragment);
  }

  @Override
  protected <V> NonBlockingReadAction<V> prepareReadAction(@NotNull NonBlockingReadAction<V> action) {
    return action.withDocumentsCommitted(project);
  }

  protected abstract @NotNull PsiCodeFragment getPsiCodeFragment(@NotNull String text);

  private static boolean areCodeFragmentsEqual(@NotNull String code1, @NotNull String code2, @NotNull Parser parser) {
    return PsiEquivalenceUtil.areElementsEquivalent(parser.parse(code1), parser.parse(code2));
  }

  private interface Parser {
    @NotNull PsiCodeFragment parse(@NotNull String text);
  }
}
