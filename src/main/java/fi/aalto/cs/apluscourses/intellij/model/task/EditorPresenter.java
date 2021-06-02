package fi.aalto.cs.apluscourses.intellij.model.task;

import fi.aalto.cs.apluscourses.ui.ideactivities.ComponentDatabase;
import fi.aalto.cs.apluscourses.ui.ideactivities.EditorHighlighter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditorPresenter extends IntelliJComponentPresenterBase {
  public EditorPresenter(@NotNull String instruction, @NotNull String info) {
    super(instruction, info);
  }

  @Override
  protected @Nullable EditorHighlighter getHighlighter() {
    return new EditorHighlighter(ComponentDatabase.getEditorWindow());
  }
}
