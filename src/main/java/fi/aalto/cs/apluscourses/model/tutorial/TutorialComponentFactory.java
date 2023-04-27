package fi.aalto.cs.apluscourses.model.tutorial;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TutorialComponentFactory {
  @NotNull TutorialComponent createEditor(@Nullable Path path, @Nullable TutorialComponent parent);

  @NotNull TutorialComponent createWindow(@Nullable TutorialComponent parent);

  @NotNull TutorialComponent createProjectTree(@Nullable TutorialComponent parent);

  @NotNull TutorialComponent createEditorBlock(@NotNull LineRange lineRange,
                                               @Nullable TutorialComponent parent);

  @NotNull TutorialComponent createBuildButton(@Nullable TutorialComponent parent);

  @NotNull TutorialComponent createRunLineButton(int line, @Nullable TutorialComponent parent);

  @NotNull TutorialComponent createRunWindow(@Nullable TutorialComponent parent);
}
