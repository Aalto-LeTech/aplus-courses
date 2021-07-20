package fi.aalto.cs.apluscourses.intellij.model.task;

import fi.aalto.cs.apluscourses.ui.ideactivities.ComponentDatabase;
import fi.aalto.cs.apluscourses.ui.ideactivities.GenericHighlighter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProjectTreePresenter extends IntelliJComponentPresenterBase {
  public ProjectTreePresenter(@NotNull String instruction, @NotNull String info) {
    super(instruction, info);
  }

  @Override
  protected @Nullable GenericHighlighter getHighlighter() {
    return new GenericHighlighter(ComponentDatabase.getProjectPane());
  }
}
