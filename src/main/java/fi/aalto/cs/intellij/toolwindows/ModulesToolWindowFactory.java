package fi.aalto.cs.intellij.toolwindows;

import com.intellij.openapi.project.Project;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

public class ModulesToolWindowFactory extends BaseToolWindowFactory {

  @Override
  protected JComponent createToolWindowContentInternal(@NotNull Project project) {
    ModulesForm modulesForm = new ModulesForm();
    modulesForm.init();
    return modulesForm.getBasePanel();
  }
}
