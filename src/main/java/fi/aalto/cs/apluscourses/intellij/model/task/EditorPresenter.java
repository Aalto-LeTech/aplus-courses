package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.ui.ideactivities.ComponentDatabase;
import fi.aalto.cs.apluscourses.ui.ideactivities.EditorHighlighter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditorPresenter extends IntelliJComponentPresenterBase {
  @NotNull
  private final String path;

  public EditorPresenter(@NotNull String instruction,
                         @NotNull String info,
                         @NotNull Project project,
                         @NotNull String path) {
    super(instruction, info, project);
    this.path = path;
  }

  @NotNull
  public static EditorPresenter create(@NotNull String instruction,
                                       @NotNull String info,
                                       @NotNull Project project,
                                       @NotNull Arguments actionArguments) {
    return new EditorPresenter(instruction, info, project, actionArguments.getString("filePath"));
  }

  @Override
  protected @Nullable EditorHighlighter getHighlighter() {
    var component = ComponentDatabase.getEditorWindow();
    return component != null ? new EditorHighlighter(component) : null;
  }

  @Override
  public boolean tryToShow() {
    return ComponentDatabase.showFile(path, project);
  }
}
