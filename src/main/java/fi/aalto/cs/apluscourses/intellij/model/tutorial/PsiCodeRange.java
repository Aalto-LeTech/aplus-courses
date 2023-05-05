package fi.aalto.cs.apluscourses.intellij.model.tutorial;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.util.Selector;
import fi.aalto.cs.apluscourses.model.tutorial.CodeRange;
import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PsiCodeRange<T extends PsiElement> extends CodeRange {
  private final @NotNull Supplier<@Nullable PsiFile> psiFileProvider;
  private final @NotNull Selector<PsiElement> selector;
  private long modStamp = -1L;
  private int startIncl = 0;
  private int endExcl = 0;

  private void recalculate() {
    Optional.ofNullable(psiFileProvider.get()).filter(this::hasPsiFileChanged).ifPresent(this::recalculate);
  }

  public PsiCodeRange(@NotNull Selector<PsiElement> selector,
                      @NotNull Supplier<PsiFile> psiFileProvider) {
    this.selector = selector;
    this.psiFileProvider = psiFileProvider;
  }

  private void recalculate(@NotNull PsiFile psiFile) {
    var psiElementOptional = selector.select(psiFile);
    if (psiElementOptional.isEmpty()) {
      startIncl = 0;
      endExcl = 0;
    } else {
      var textRange = psiElementOptional.get().getTextRange();
      startIncl = textRange.getStartOffset();
      endExcl = textRange.getEndOffset();
    }
    modStamp = psiFile.getModificationStamp();
  }

  @Override
  public int getStartInclusive() {
    recalculate();
    return startIncl;
  }

  @Override
  public int getEndExclusive() {
    recalculate();
    return endExcl;
  }

  private boolean hasPsiFileChanged(@NotNull PsiFile psiFile) {
    return psiFile.getModificationStamp() != modStamp;
  }
}
