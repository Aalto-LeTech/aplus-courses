package fi.aalto.cs.apluscourses.model;

public class TutorialExercise extends Exercise {

  private final Tutorial tutorial;

  public TutorialExercise() {
    super(342405, "Assignment 1 (Tutorial)", "", 0, 0, 0, true);
    this.tutorial = new Tutorial();
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
    return obj instanceof TutorialExercise && ((TutorialExercise) obj).getId() == getId();
  }

}
