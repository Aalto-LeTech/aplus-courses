package fi.aalto.cs.apluscourses.model.tutorial.parser;

import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import fi.aalto.cs.apluscourses.model.tutorial.switching.SceneSwitch;
import fi.aalto.cs.apluscourses.model.tutorial.switching.StateSwitch;
import org.jetbrains.annotations.NotNull;

public interface TutorialScope {
  @NotNull TutorialComponent getComponent();

  @NotNull StateSwitch getStateSwitch();

  @NotNull SceneSwitch getSceneSwitch();
}
