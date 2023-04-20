package fi.aalto.cs.apluscourses.intellij.model.tutorial;

import com.intellij.openapi.editor.Document;
import fi.aalto.cs.apluscourses.model.tutorial.LineRange;
import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJDocumentLineRange extends LineRange {

  private final Supplier<@Nullable Document> documentSupplier;
  private final LineRange lineRange;

  public IntelliJDocumentLineRange(@NotNull Supplier<@Nullable Document> documentSupplier,
                                   @Nullable LineRange lineRange) {
    this.documentSupplier = documentSupplier;
    this.lineRange = lineRange;
  }

  @Override
  public int getFirst() {
    return Optional.ofNullable(lineRange).map(LineRange::getFirst).orElse(1);
  }

  @Override
  public int getLast() {
    var doc = documentSupplier.get();
    if (doc == null && lineRange == null) {
      return 0;
    }
    if (doc == null) {
      return lineRange.getLast();
    }
    if (lineRange == null) {
      return doc.getLineCount();
    }
    return Math.min(lineRange.getLast(), doc.getLineCount());
  }
}
