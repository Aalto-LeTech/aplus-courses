package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;

public class TutorialExercise extends Exercise {

  private final @NotNull Tutorial tutorial;

  public TutorialExercise(long id,
                          @NotNull String name,
                          @NotNull String htmlUrl,
                          int userPoints,
                          @NotNull Tutorial tutorial) {
    super(id, name, htmlUrl, userPoints, 1, 1, false /* TODO change to true (submittable) */);
    this.tutorial = tutorial;
  }

  public Tutorial getTutorial() {
    return tutorial;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(getId());
  }

  @Override
  public boolean equals(Object obj) {
    /* Honestly, this could be removed even if linters probably complained.
     * We want two exercises to be considered equal if their ids match no matter their types.  */
    return obj instanceof TutorialExercise && ((TutorialExercise) obj).getId() == getId();
  }

}
