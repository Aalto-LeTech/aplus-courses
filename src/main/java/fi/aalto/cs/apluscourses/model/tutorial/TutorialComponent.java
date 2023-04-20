package fi.aalto.cs.apluscourses.model.tutorial;

import org.jetbrains.annotations.NotNull;

public interface TutorialComponent {

  default @NotNull CodeContext getCodeContext() {
    throw new UnsupportedOperationException("The component does not provide a code context.");
  }

}
