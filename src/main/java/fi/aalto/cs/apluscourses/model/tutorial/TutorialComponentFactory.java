package fi.aalto.cs.apluscourses.model.tutorial;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TutorialComponentFactory {
  @NotNull TutorialComponent createEditor(@Nullable Path path, @Nullable TutorialComponent parent);

  @NotNull TutorialComponent createWindow(@Nullable TutorialComponent parent);

  @NotNull TutorialComponent createProjectTree(@Nullable TutorialComponent parent);

  @NotNull TutorialComponent createEditorBlock(@NotNull CodeRange codeRange,
                                               @Nullable TutorialComponent parent);

  @NotNull TutorialComponent createBuildButton(@Nullable TutorialComponent parent);

  @NotNull TutorialComponent createRunLineButton(@NotNull CodeRange codeRange, @Nullable TutorialComponent parent);

  @NotNull TutorialComponent createRunWindow(@Nullable TutorialComponent parent);

  @NotNull TutorialComponent createLineBreakpointButton(@NotNull CodeRange codeRange,
                                                        @Nullable TutorialComponent parent);

  @NotNull TutorialComponent createBalloon(@Nullable TutorialComponent parent);
}
