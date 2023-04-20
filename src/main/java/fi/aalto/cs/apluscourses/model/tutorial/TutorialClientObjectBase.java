package fi.aalto.cs.apluscourses.model.tutorial;

import org.jetbrains.annotations.NotNull;

public abstract class TutorialClientObjectBase implements TutorialClientObject {
  private final @NotNull TutorialComponent component;

  protected TutorialClientObjectBase(@NotNull TutorialComponent component) {
    this.component = component;
  }

  @Override
  public @NotNull TutorialComponent getComponent() {
    return component;
  }
}
