package fi.aalto.cs.apluscourses.intellij.actions;

import fi.aalto.cs.apluscourses.presentation.TutorialCommand;

public class TutorialAction extends CommandAction {
  public TutorialAction() {
    super(new TutorialCommand());
  }
}
