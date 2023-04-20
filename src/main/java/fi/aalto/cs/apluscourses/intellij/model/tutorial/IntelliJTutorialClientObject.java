package fi.aalto.cs.apluscourses.intellij.model.tutorial;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.component.IntelliJTutorialComponent;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialClientObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IntelliJTutorialClientObject extends TutorialClientObject {
  default @NotNull IntelliJTutorialComponent<?> getIntelliJComponent() {
    return (IntelliJTutorialComponent<?>) getComponent();
  }

  default @Nullable Project getProject() {
    return getIntelliJComponent().getProject();
  }
}
