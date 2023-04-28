package fi.aalto.cs.apluscourses.intellij.model.tutorial.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.SyntaxTraverser;
import com.intellij.util.containers.TreeTraversal;
import fi.aalto.cs.apluscourses.utils.CollectionUtil;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.lang.model.SourceVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaPsiElement;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScFor;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScIf;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScTry;

public class PsiSelector<T extends PsiElement> extends SelectorBase<PsiElement> {
  public static final Pattern PATTERN = Pattern.compile("^(?<id>.*?)\\[(?<index>.*)]$");

  private final Class<T> clazz;

  private final int index;

  public PsiSelector(@NotNull Class<T> clazz, int index, @NotNull Selector<PsiElement> subSelector) {
    super(subSelector);
    this.clazz = clazz;
    this.index = index;
  }

  public static @NotNull PsiSelector<?> parse(@NotNull String string) {
    String s;
    Selector<PsiElement> subSelector;
    int indexOfGt = string.indexOf(">");
    if (indexOfGt < 0) {
      s = string;
      subSelector = SelfSelector.getInstance();
    } else {
      s = string.substring(0, indexOfGt);
      subSelector = parse(string.substring(indexOfGt + 1));
    }
    return parse(s, subSelector);
  }

  public static @NotNull PsiSelector<?> parse(@NotNull String string, @NotNull Selector<PsiElement> subSelector) {
    var matcher = PATTERN.matcher(string);
    String id;
    int index;
    if (matcher.matches()) {
      id = matcher.group("id");
      index = Integer.parseInt(matcher.group("index"));
    } else {
      id = string;
      index = 0;
    }
    switch (id) {
      case "for":
        return new PsiSelector<>(ScFor.class, index, subSelector);
      case "if":
        return new PsiSelector<>(ScIf.class, index, subSelector);
      case "try":
        return new PsiSelector<>(ScTry.class, index, subSelector);
      case "_":
        return new PsiSelector<>(ScalaPsiElement.class, index, subSelector);
      default:
        if (SourceVersion.isIdentifier(string)) {
          return new NamedSelector(string, subSelector);
        }
        throw new IllegalArgumentException("Cannot parse: " + string);
    }
  }

  @Override
  protected @NotNull Stream<? extends @NotNull PsiElement> stream(@NotNull PsiElement elem) {
    return CollectionUtil.ofType(clazz,
        StreamSupport.stream(
            SyntaxTraverser
                .psiTraverser(elem)
                .withTraversal(TreeTraversal.PLAIN_BFS)
                .spliterator(),
            false))
        .filter(this::test)
        .skip(index);
  }

  protected boolean test(@NotNull T elem) {
    return true;
  }

  protected static class NamedSelector extends PsiSelector<PsiNamedElement> {

    private final @NotNull String name;

    protected NamedSelector(@NotNull String name, @NotNull Selector<PsiElement> subSelector) {
      super(PsiNamedElement.class, 0, subSelector);
      this.name = name;
    }

    @Override
    protected boolean test(@NotNull PsiNamedElement elem) {
      return name.equals(elem.getName());
    }
  }
}
