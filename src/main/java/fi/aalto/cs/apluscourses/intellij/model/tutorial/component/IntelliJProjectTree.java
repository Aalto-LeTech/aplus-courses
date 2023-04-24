package fi.aalto.cs.apluscourses.intellij.model.tutorial.component;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.openapi.project.Project;
import java.awt.Component;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class IntelliJProjectTree extends IntelliJTutorialComponent<Component> {
  public IntelliJProjectTree(@Nullable Project project) {
    super(project);
  }

  @Override
  protected @Nullable Component getAwtComponent() {
    return Optional.ofNullable(getProject())
        .map(ProjectView::getInstance)
        .map(ProjectView::getCurrentProjectViewPane)
        .map(AbstractProjectViewPane::getTree)
        .orElse(null);
  }
}
