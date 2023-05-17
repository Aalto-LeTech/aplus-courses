package fi.aalto.cs.apluscourses.intellij.model.tutorial.component;

import com.intellij.openapi.project.Project;
import com.intellij.ui.BalloonImpl;
import fi.aalto.cs.apluscourses.intellij.utils.IntelliJUIUtil;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import java.util.Optional;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nullable;

public class IntelliJBalloon extends IntelliJTutorialComponent<JComponent> {
  public IntelliJBalloon(@Nullable TutorialComponent parent, @Nullable Project project) {
    super(parent, project);
  }

  @Override
  protected @Nullable JComponent getAwtComponent() {
    return Optional.ofNullable(getProject())
        .map(IntelliJUIUtil::getBalloon)
        .map(BalloonImpl::getComponent)
        .orElse(null);
  }
}
