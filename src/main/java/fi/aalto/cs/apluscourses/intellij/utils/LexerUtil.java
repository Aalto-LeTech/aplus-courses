package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.lexer.TokenList;
import com.intellij.psi.tree.TokenSet;
import fi.aalto.cs.apluscourses.utils.CollectionUtil;
import fi.aalto.cs.apluscourses.utils.EqualityComparator;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class LexerUtil {
  public static boolean matches(@NotNull TokenList tokenList1,
                                @NotNull TokenList tokenList2,
                                @NotNull TokenSet toSkip) {
    return CollectionUtil.equals(new SkippingTokenList(tokenList1, toSkip),
                                 new SkippingTokenList(tokenList2, toSkip),
                                 EqualityComparator.from(CharSequence::compare));
  }

  private static class SkippingTokenList implements Iterable<@NotNull CharSequence> {
    private final @NotNull TokenList tokenList;
    private final @NotNull TokenSet toSkip;

    public SkippingTokenList(@NotNull TokenList tokenList, @NotNull TokenSet toSkip) {
      this.tokenList = tokenList;
      this.toSkip = toSkip;
    }

    @Override
    public @NotNull Iterator<@NotNull CharSequence> iterator() {
      return new MyIterator();
    }

    private class MyIterator implements Iterator<@NotNull CharSequence> {
      private int index = tokenList.forwardWhile(0, toSkip);

      @Override
      public boolean hasNext() {
        return index < tokenList.getTokenCount();
      }

      @Override
      public @NotNull CharSequence next() {
        var token = tokenList.getTokenText(index);
        index = tokenList.forwardWhile(index + 1, toSkip);
        return token;
      }
    }
  }
}
