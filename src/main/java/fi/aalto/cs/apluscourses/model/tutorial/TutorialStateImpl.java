package fi.aalto.cs.apluscourses.model.tutorial;

import fi.aalto.cs.apluscourses.utils.ArrayUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class TutorialStateImpl implements TutorialState {
  private final @NotNull String key;
  private final @NotNull TutorialScene @NotNull [] scenes;
  private final @NotNull List<@NotNull TutorialClientObject> objects;
  private int currentIndex = 0;

  public TutorialStateImpl(@NotNull String key,
                           @NotNull List<@NotNull TutorialScene> scenes,
                           @NotNull Collection<@NotNull TutorialClientObject> objects) {
    this.key = key;
    this.scenes = scenes.isEmpty() ? new TutorialScene[] { new TutorialScene() } : scenes.toArray(TutorialScene[]::new);
    this.objects = List.copyOf(objects);
  }

  @Override
  public @NotNull String getKey() {
    return key;
  }

  @Override
  public void activate() {
    objects.forEach(TutorialObject::activate);
    scenes[currentIndex].activate();
  }

  @Override
  public void deactivate() {
    objects.forEach(TutorialObject::deactivate);
    scenes[currentIndex].deactivate();
  }

  private void goToScene(int index) {
    if (!ArrayUtil.containsIndex(scenes, index)) {
      throw new IndexOutOfBoundsException("No such tutorial scene: " + index);
    }
    scenes[currentIndex].deactivate();
    currentIndex = index;
    scenes[currentIndex].activate();
  }

  private boolean canGoToScene(int index) {
    return ArrayUtil.containsIndex(scenes, index);
  }

  @Override
  public void goForward() {
    goToScene(currentIndex + 1);
  }

  @Override
  public void goBackward() {
    goToScene(currentIndex - 1);
  }

  @Override
  public boolean canGoForward() {
    return canGoToScene(currentIndex + 1);
  }

  @Override
  public boolean canGoBackward() {
    return canGoToScene(currentIndex - 1);
  }
}
