package fi.aalto.cs.apluscourses.model.tutorial.parser;

import fi.aalto.cs.apluscourses.model.tutorial.Tutorial;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import fi.aalto.cs.apluscourses.utils.dom.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TutorialParser {
  public @Nullable Tutorial parse(@NotNull Node rootNode, @NotNull TutorialComponent component);
}
