package fi.aalto.cs.apluscourses.model.tutorial;

import org.jetbrains.annotations.NotNull;

public interface TutorialClientObject extends TutorialObject {
  @NotNull TutorialComponent getComponent();
}
