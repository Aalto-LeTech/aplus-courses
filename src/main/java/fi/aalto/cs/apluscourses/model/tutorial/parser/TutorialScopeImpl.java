package fi.aalto.cs.apluscourses.model.tutorial.parser;

import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import fi.aalto.cs.apluscourses.model.tutorial.switching.SceneSwitch;
import fi.aalto.cs.apluscourses.model.tutorial.switching.SceneSwitchImpl;
import fi.aalto.cs.apluscourses.model.tutorial.switching.StateSwitch;
import fi.aalto.cs.apluscourses.model.tutorial.switching.StateSwitchImpl;
import org.jetbrains.annotations.NotNull;

public class TutorialScopeImpl implements TutorialScope {
  private final @NotNull TutorialComponent component;
  private final @NotNull StateSwitch stateSwitch;
  private final @NotNull SceneSwitch sceneSwitch;

  public TutorialScopeImpl(@NotNull TutorialComponent component) {
    this.component = component;
    this.stateSwitch = new StateSwitchImpl();
    this.sceneSwitch = new SceneSwitchImpl();
  }

  public TutorialScopeImpl(@NotNull TutorialComponent component, @NotNull StateSwitch stateSwitch, @NotNull SceneSwitch sceneSwitch) {
    this.component = component;
    this.stateSwitch = stateSwitch;
    this.sceneSwitch = sceneSwitch;
  }

  @Override
  public @NotNull TutorialComponent getComponent() {
    return component;
  }

  public @NotNull StateSwitch getStateSwitch() {
    return stateSwitch;
  }

  @Override
  public @NotNull SceneSwitch getSceneSwitch() {
    return sceneSwitch;
  }
}
