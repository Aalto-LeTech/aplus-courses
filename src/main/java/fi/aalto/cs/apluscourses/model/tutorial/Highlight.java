package fi.aalto.cs.apluscourses.model.tutorial;

import org.jetbrains.annotations.NotNull;

public abstract class Highlight extends TutorialClientObjectBase {
  private final @NotNull Degree degree;

  protected Highlight(@NotNull Degree degree, @NotNull TutorialComponent component) {
    super(component);
    this.degree = degree;
  }

  public Degree getDegree() {
    return degree;
  }

  public enum Degree {
    dim, normal, focus
  }
}
