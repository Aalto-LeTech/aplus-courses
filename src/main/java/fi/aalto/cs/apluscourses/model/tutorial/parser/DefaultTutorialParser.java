package fi.aalto.cs.apluscourses.model.tutorial.parser;

import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialFactory;
import fi.aalto.cs.apluscourses.model.tutorial.switching.SceneSwitch;
import fi.aalto.cs.apluscourses.model.tutorial.switching.StateSwitch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultTutorialParser extends TutorialParserBase<TutorialScope> {

  public DefaultTutorialParser(@NotNull TutorialFactory factory) {
    super(factory);
  }

  @Override
  protected @NotNull TutorialScope createModifiedScope(@Nullable TutorialComponent component,
                                                       @Nullable StateSwitch stateSwitch,
                                                       @Nullable SceneSwitch sceneSwitch,
                                                       @NotNull TutorialScope scope) {
    return new TutorialScopeImpl(
        component == null ? scope.getComponent() : component,
        stateSwitch == null ? scope.getStateSwitch() : stateSwitch,
        sceneSwitch == null ? scope.getSceneSwitch() : sceneSwitch);
  }

  @Override
  protected @NotNull TutorialScope createInitialScope(@NotNull TutorialComponent component) {
    return new TutorialScopeImpl(component);
  }
}
