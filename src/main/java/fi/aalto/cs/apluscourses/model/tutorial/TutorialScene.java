package fi.aalto.cs.apluscourses.model.tutorial;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class TutorialScene implements TutorialObject {
  private final @NotNull List<TutorialClientObject> objects;

  public TutorialScene(@NotNull Collection<TutorialClientObject> objects) {
    this.objects = List.copyOf(objects);
  }

  public TutorialScene() {
    this.objects = Collections.emptyList();
  }

  public void activate() {
    objects.forEach(TutorialObject::activate);
  }

  public void deactivate() {
    objects.forEach(TutorialObject::deactivate);
  }
}
