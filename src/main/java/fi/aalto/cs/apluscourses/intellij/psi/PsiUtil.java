package fi.aalto.cs.apluscourses.intellij.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.SyntaxTraverser;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;


public class PsiUtil {
  private PsiUtil() {

  }

  public static boolean psiSiblingHasErrors(@NotNull PsiElement element) {
    return psiElementHasErrors(element.getPrevSibling()) || psiElementHasErrors(element.getNextSibling());
  }

  public static boolean psiNextSiblingHasErrors(@NotNull PsiElement element) {
    return psiElementHasErrors(element.getNextSibling());
  }

  public static boolean psiElementHasErrors(@NotNull PsiElement element) {
    return !SyntaxTraverser.psiTraverser(element).traverse().filter(PsiErrorElement.class).isEmpty();
  }
  
  /**
   * Gets all the siblings of the given PsiElement.
   */
  public static Collection<PsiElement> getPsiElementsSiblings(PsiElement methodElement) {
    List<PsiElement> elements = new ArrayList<>();

    PsiElement prevSibling = methodElement.getPrevSibling();
    PsiElement nextSibling = methodElement;
  
    //First make sure to find the leftmost sibling (in order to maintain the order of the elements)
    while (prevSibling != null) {
      nextSibling = prevSibling;
      prevSibling = prevSibling.getPrevSibling();
    }
  
    while (nextSibling != null) {
      if (nextSibling instanceof PsiWhiteSpace) {
        nextSibling = nextSibling.getNextSibling();
        continue;
      } else if (nextSibling.getChildren().length > 0) {
        elements.addAll(getPsiElementsSiblings(nextSibling.getChildren()[0]));
      } else {
        elements.add(nextSibling);
      }
      nextSibling = nextSibling.getNextSibling();
    }
  
    return elements;
  }
  
  /**
   * Finds the next PsiElement sibling that is not a PsiWhiteSpace.
   */
  public static PsiElement findNextNonEmptyPsiElement(Optional<PsiElement> elementOptional) {
    if (elementOptional.isPresent()) {
      PsiElement element = elementOptional.get();
      while (element.getNextSibling() != null) {
        element = element.getNextSibling();
        if (!(element instanceof PsiWhiteSpace)) {
          return element;
        }
      }
    }
    return null;
  }
  
  /**
   * Finds the next LeafPsiElement sibling that is not a PsiWhiteSpace.
   */
  public static PsiElement findNextLeafPsiElement(Optional<PsiElement> elementOptional) {
    if (elementOptional.isPresent()) {
      PsiElement element = elementOptional.get();
      while (element.getNextSibling() != null) {
        element = element.getNextSibling();
        if (element instanceof LeafPsiElement
            && !(element instanceof PsiWhiteSpace)) {
          return element;
        }
      }
    }
    return null;
  }
}

