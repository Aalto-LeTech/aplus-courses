package fi.aalto.cs.apluscourses.intellij.model.tutorial.component;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import fi.aalto.cs.apluscourses.intellij.utils.IntelliJUIUtil;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import java.util.Optional;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJBalloon extends IntelliJTutorialComponent<JPanel> {
  public IntelliJBalloon(@Nullable TutorialComponent parent, @Nullable Project project) {
    super(parent, project);
  }

  @Override
  protected @Nullable JPanel getAwtComponent() {
    return Optional.ofNullable(WindowManager.getInstance().getFrame(getProject()))
        .map(IntelliJUIUtil::getBalloonPanel)
        .orElse(null);
  }
}
