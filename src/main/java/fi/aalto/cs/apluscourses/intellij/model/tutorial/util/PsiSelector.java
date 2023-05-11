package fi.aalto.cs.apluscourses.intellij.model.tutorial.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.SyntaxTraverser;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.containers.TreeTraversal;
import fi.aalto.cs.apluscourses.intellij.psi.PsiUtil;
import fi.aalto.cs.apluscourses.utils.CollectionUtil;
import fi.aalto.cs.apluscourses.utils.CustomIterable;
import fi.aalto.cs.apluscourses.utils.StringUtil;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.lang.model.SourceVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaPsiElement;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScFor;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScIf;
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScTry;

public class PsiSelector<T extends PsiElement> extends SelectorBase<PsiElement> {
  public static final Pattern PATTERN = Pattern.compile("^(?<ref>@?)(?<id>.*?)(?:\\[(?<index>.*)])?$");

  public static final char AXIS_DESCEND = '>';
  public static final char AXIS_SIBLING = '~';

  private final int axis;
  private final Class<T> clazz;
  private final int index;

  public PsiSelector(@NotNull Class<T> clazz, int axis, int index, @NotNull Selector<PsiElement> subSelector) {
    super(subSelector);
    this.axis = axis;
    this.clazz = clazz;
    this.index = index;
  }

  public static @NotNull PsiSelector<?> parse(@NotNull String string) {
    return parse(string, AXIS_DESCEND);
  }

  public static @NotNull PsiSelector<?> parse(@NotNull String string, char axis) {
    String s;
    Selector<PsiElement> subSelector;
    int indexOfAxis = StringUtil.indexOfAny(string, AXIS_DESCEND, AXIS_SIBLING);
    if (indexOfAxis < 0) {
      s = string;
      subSelector = SelfSelector.getInstance();
    } else {
      s = string.substring(0, indexOfAxis);
      subSelector = parse(string.substring(indexOfAxis + 1), string.charAt(indexOfAxis));
    }
    return parse(s, axis, subSelector);
  }

  public static @NotNull PsiSelector<?> parse(@NotNull String string,
                                              char axis,
                                              @NotNull Selector<PsiElement> subSelector) {
    var matcher = PATTERN.matcher(string);
    boolean ref;
    String id;
    int index;
    if (matcher.matches()) {
      ref = !matcher.group("ref").isEmpty();
      id = matcher.group("id");
      index = matcher.group("index") == null ? 0 : Integer.parseInt(matcher.group("index"));
    } else {
      ref  = false;
      id = string;
      index = 0;
    }
    switch (id) {
      case "for":
        return new PsiSelector<>(ScFor.class, axis, index, subSelector);
      case "if":
        return new PsiSelector<>(ScIf.class, axis, index, subSelector);
      case "try":
        return new PsiSelector<>(ScTry.class, axis, index, subSelector);
      case "_":
        return new PsiSelector<>(ScalaPsiElement.class, axis, index, subSelector);
      default:
        if (SourceVersion.isIdentifier(id)) {
          return ref ? new RefSelector(id, axis, subSelector) : new NamedSelector(id, axis, subSelector);
        }
        throw new IllegalArgumentException("Cannot parse: " + string);
    }
  }

  @Override
  protected @NotNull Stream<? extends @NotNull PsiElement> stream(@NotNull PsiElement root) {
    return CollectionUtil.ofType(clazz, streamInternal(root))
        .skip(1) // do not include root
        .filter(this::test)
        .skip(index);
  }

  private @NotNull Stream<? extends @NotNull PsiElement> streamInternal(@NotNull PsiElement root) {
    switch (axis) {
      case AXIS_DESCEND:
        return streamDescend(root);
      case AXIS_SIBLING:
        return streamSibling(root);
      default:
        throw new IllegalArgumentException("Unknown axis: " + axis);
    }
  }

  private @NotNull Stream<? extends @NotNull PsiElement> streamDescend(@NotNull PsiElement root) {
    return CollectionUtil.stream(
          SyntaxTraverser
            .psiTraverser(root)
            .withTraversal(TreeTraversal.PLAIN_BFS)
            .forceDisregardTypes(TokenSet.WHITE_SPACE::contains)
        );
  }

  private @NotNull Stream<? extends @NotNull PsiElement> streamSibling(@NotNull PsiElement root) {
    return CollectionUtil.stream(
          CustomIterable.from(root, PsiElement::getNextSibling)
        )
        .filter(PsiUtil::isNotWhitespace);
  }

  protected boolean test(@NotNull T elem) {
    return true;
  }

  protected static class RefSelector extends PsiSelector<PsiElement> {

    private final @NotNull String text;

    public RefSelector(@NotNull String text, char axis, @NotNull Selector<PsiElement> subSelector) {
      super(PsiElement.class, axis, 0, subSelector);
      this.text = text;
    }

    @Override
    protected boolean test(@NotNull PsiElement elem) {
      return elem.textMatches(text);
    }
  }

  protected static class NamedSelector extends PsiSelector<PsiNamedElement> {

    private final @NotNull String name;

    protected NamedSelector(@NotNull String name, char axis, @NotNull Selector<PsiElement> subSelector) {
      super(PsiNamedElement.class, axis, 0, subSelector);
      this.name = name;
    }

    @Override
    protected boolean test(@NotNull PsiNamedElement elem) {
      return name.equals(elem.getName());
    }
  }
}
