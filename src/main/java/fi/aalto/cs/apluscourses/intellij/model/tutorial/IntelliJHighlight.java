package fi.aalto.cs.apluscourses.intellij.model.tutorial;

import fi.aalto.cs.apluscourses.intellij.model.tutorial.component.IntelliJTutorialComponent;
import fi.aalto.cs.apluscourses.model.tutorial.Highlight;
import fi.aalto.cs.apluscourses.ui.tutorials.OverlayPane;
import org.jetbrains.annotations.NotNull;

public class IntelliJHighlight extends Highlight implements IntelliJTutorialClientObject {
  private final OverlayPane overlayPane;

  public IntelliJHighlight(@NotNull Highlight.Degree degree,
                           @NotNull IntelliJTutorialComponent<?> component,
                           @NotNull OverlayPane overlayPane) {
    super(degree, component);
    this.overlayPane = overlayPane;
  }

  @Override
  public void activate() {
    overlayPane.addHighlight(getDegree(), getIntelliJComponent());
  }

  @Override
  public void deactivate() {
    overlayPane.removeHighlight(getDegree(), getIntelliJComponent());
  }
}
