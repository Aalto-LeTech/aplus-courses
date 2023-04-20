package fi.aalto.cs.apluscourses.intellij.model.tutorial.observer;

import com.intellij.lang.impl.TokenSequence;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.TokenList;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.tree.TokenSet;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.IntelliJTutorialClientObject;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.component.IntelliJTutorialComponent;
import fi.aalto.cs.apluscourses.intellij.utils.LexerUtil;
import fi.aalto.cs.apluscourses.model.tutorial.CodeContext;
import fi.aalto.cs.apluscourses.model.tutorial.Observer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.scala.lang.lexer.ScalaLexer;
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes;

public class IntelliJCodeObserver extends Observer implements IntelliJTutorialClientObject {
  private @Nullable Document activeDocument;
  private final @NotNull CodeChecker codeChecker;
  private final @NotNull CodeLocator codeLocator;
  private final @NotNull DocumentListener listener;

  public IntelliJCodeObserver(@NotNull String lang,
                              @NotNull String code,
                              @NotNull IntelliJTutorialComponent<?> component) {
    this(TokenCodeChecker.create(code, lang, component.getProject()),
        new ContextAwareCodeLocator(component.getCodeContext()),
        component);
  }

  protected IntelliJCodeObserver(@NotNull CodeChecker codeChecker,
                                 @NotNull CodeLocator codeLocator,
                                 @NotNull IntelliJTutorialComponent<?> component) {
    super(component);
    this.codeChecker = codeChecker;
    this.codeLocator = codeLocator;
    listener = new MyListener();
  }

  @Override
  public void activate() {
    var document = Objects.requireNonNull(getIntelliJComponent().getDocument(), "Document not open.");
    activeDocument = document;
    document.addDocumentListener(listener);
  }

  @Override
  public void deactivate() {
    var document = Objects.requireNonNull(activeDocument, "No active document");
    document.removeDocumentListener(listener);
    activeDocument = null;
  }

  private class MyListener implements DocumentListener {
    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
      if (codeChecker.check(codeLocator.getCode(event.getDocument()))) {
        fire();
      }
    }
  }

  @FunctionalInterface
  protected interface CodeChecker {
    boolean check(@NotNull CharSequence code);
  }

  @FunctionalInterface
  protected interface CodeLocator {
    @NotNull CharSequence getCode(@NotNull Document document);
  }

  private static class ContextAwareCodeLocator implements CodeLocator {

    private final @NotNull CodeContext codeContext;

    private ContextAwareCodeLocator(@NotNull CodeContext codeContext) {
      this.codeContext = codeContext;
    }

    @Override
    public @NotNull CharSequence getCode(@NotNull Document document) {
      var lineRange = codeContext.getLineRange();
      var firstLine = lineRange.getFirst() - 1;
      var lastLine = lineRange.getLast() - 1;
      var start = document.getLineStartOffset(firstLine);
      var end = document.getLineEndOffset(lastLine);
      return document.getText(TextRange.create(start, end));
    }
  }

  @SuppressWarnings("UnstableApiUsage")
  protected static class TokenCodeChecker implements CodeChecker {
    private final @NotNull TokenList tokenList;
    private final @NotNull Lexer lexer;
    private final @NotNull TokenSet toSkip;

    public TokenCodeChecker(@NotNull CharSequence expectedCode, @NotNull Lexer lexer, @NotNull TokenSet toSkip) {
      this.tokenList = TokenSequence.performLexing(expectedCode, lexer);
      this.lexer = lexer;
      this.toSkip = toSkip;
    }

    public static @NotNull TokenCodeChecker create(@NotNull String code,
                                                   @NotNull String lang,
                                                   @Nullable Project project) {
      switch (lang) {
        case "scala3":
          return new TokenCodeChecker(code, new ScalaLexer(true, project),
              ScalaTokenTypes.WHITES_SPACES_AND_COMMENTS_TOKEN_SET);
        default:
          throw new IllegalArgumentException("Unsupported language: " + lang);
      }
    }

    @Override
    public boolean check(@NotNull CharSequence code) {
      return LexerUtil.matches(tokenList, TokenSequence.performLexing(code, lexer), toSkip);
    }
  }
}
