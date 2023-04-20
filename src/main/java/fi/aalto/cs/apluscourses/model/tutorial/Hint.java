package fi.aalto.cs.apluscourses.model.tutorial;

import fi.aalto.cs.apluscourses.model.tutorial.switching.SceneSwitch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Hint extends TutorialClientObjectBase {

  private final @Nullable String title;
  private final @NotNull String content;

  protected Hint(@Nullable String title, @NotNull String content, @NotNull TutorialComponent component) {
    super(component);
    this.title = title;
    this.content = content;
  }

  @Override
  public abstract void activate();

  @Override
  public abstract void deactivate();

  public @Nullable String getTitle() {
    return title;
  }

  public @NotNull String getContent() {
    return content;
  }
}
