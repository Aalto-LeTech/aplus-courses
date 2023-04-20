package fi.aalto.cs.apluscourses.model.tutorial;

import fi.aalto.cs.apluscourses.model.tutorial.switching.SceneSwitch;
import org.jetbrains.annotations.Nullable;

public interface TutorialState extends TutorialObject, SceneSwitch {
  @Nullable String getKey();

}
