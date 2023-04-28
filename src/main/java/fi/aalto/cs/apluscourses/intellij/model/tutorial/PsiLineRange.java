package fi.aalto.cs.apluscourses.intellij.model.tutorial;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.SyntaxTraverser;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.TreeTraversal;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.component.IntelliJEditor;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.util.PsiSelector;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.util.Selector;
import fi.aalto.cs.apluscourses.model.tutorial.LineRange;
import fi.aalto.cs.apluscourses.utils.CollectionUtil;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PsiLineRange<T extends PsiElement> extends LineRange {
  private final @NotNull IntelliJEditor editorComponent;
  private final @NotNull Selector<PsiElement> selector;
  private long modStamp = -1L;
  private int first = 1;
  private int last = 0;

  private void recalculate() {
    Optional.ofNullable(editorComponent.getDocument()).filter(this::hasDocumentChanged).ifPresent(this::recalculate);
  }

  public PsiLineRange(@NotNull Selector<PsiElement> selector,
                      @NotNull IntelliJEditor editorComponent) {
    this.editorComponent = editorComponent;
    this.selector = selector;
  }

  private void recalculate(@NotNull Document document) {
    TextRange textRange = getTextRange(document);
    if (textRange == null) {
      first = 1;
      last = 0;
    } else {
      first = document.getLineNumber(textRange.getStartOffset()) + 1;
      last = document.getLineNumber(textRange.getEndOffset()) + 1;
    }
    modStamp = document.getModificationStamp();
  }

  @Override
  public int getFirst() {
    recalculate();
    return first;
  }

  @Override
  public int getLast() {
    recalculate();
    return last;
  }

  private @Nullable TextRange getTextRange(@NotNull Document document) {
    return Optional.ofNullable(editorComponent.getProject())
        .map(PsiDocumentManager::getInstance)
        .map(mgr -> mgr.getPsiFile(document))
        .map(selector::select)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(PsiElement::getTextRange)
        .orElse(null);
  }

  private boolean hasDocumentChanged(@NotNull Document document) {
    return document.getModificationStamp() != modStamp;
  }
}
