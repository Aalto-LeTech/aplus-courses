package fi.aalto.cs.apluscourses.intellij.model.tutorial.component;

import com.intellij.openapi.project.Project;
import java.awt.Component;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.Nullable;

public class IntelliJWindow extends IntelliJTutorialComponent<Component> {
  public IntelliJWindow(@Nullable Project project) {
    super(project);
  }

  @Override
  protected @Nullable Component getAwtComponent() {
    return JOptionPane.getRootFrame();
  }
}
