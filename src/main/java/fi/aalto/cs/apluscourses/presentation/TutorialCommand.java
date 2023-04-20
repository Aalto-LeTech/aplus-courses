package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.model.TutorialExercise;
import fi.aalto.cs.apluscourses.model.tutorial.Tutorial;
import fi.aalto.cs.apluscourses.presentation.base.PresentationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TutorialCommand implements Command {
  @Override
  public void execute(@NotNull PresentationContext context) {
    var tutorial = getSelectedTutorial(context);
    if (tutorial == null) {
      return;
    }
    tutorial.activate();
  }

  @Override
  public boolean canExecute(@NotNull PresentationContext context) {
    return getSelectedTutorial(context) != null;
  }

  private @Nullable Tutorial getSelectedTutorial(@NotNull PresentationContext context) {
    var exercise = context.getSelectedExercise();
    if (exercise instanceof TutorialExercise) {
      return ((TutorialExercise) exercise).getTutorial();
    }
    return null;
  }
}
