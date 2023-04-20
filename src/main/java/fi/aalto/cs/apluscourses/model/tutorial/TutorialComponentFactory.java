package fi.aalto.cs.apluscourses.model.tutorial;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TutorialComponentFactory {
  @NotNull TutorialComponent createEditor(@Nullable Path path);

  @NotNull TutorialComponent createWindow();

  @NotNull TutorialComponent createProjectTree();

  @NotNull TutorialComponent createEditorBlock(@Nullable Path path, @NotNull LineRange lineRange);
}
