package fi.aalto.cs.apluscourses.model;

import java.util.Map;

public class TutorialExercise extends Exercise {

  private final Tutorial tutorial;

  /**
   * Constructor.
   */
  public TutorialExercise() {
    // TODO: what's the submission info for a tutorial exercise?
    super(342405, "Assignment 1 (Tutorial)", "", new SubmissionInfo(Map.of()), 0, 0, 0);
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
