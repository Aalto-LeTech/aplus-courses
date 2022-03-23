package fi.aalto.cs.apluscourses.ui.module;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBViewport;
import fi.aalto.cs.apluscourses.intellij.toolwindows.APlusToolWindowFactory;
import javax.swing.Action;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModulesDialog extends DialogWrapper {
  @NotNull
  private final ModulesView modulesView;

  /**
   * Constructor.
   */
  public ModulesDialog(@NotNull Project project) {
    super(project);

    this.modulesView = APlusToolWindowFactory.createModulesView(project);

    setModal(false);

    init();
    setSize(400, 600);
  }

  @Override
  protected Action @NotNull [] createActions() {
    return new Action[0];
  }

  @Override
  @Nullable
  public JComponent getPreferredFocusedComponent() {
    return ((JComponent) ((JBViewport) ((JBScrollPane) modulesView.getBasePanel().getComponent(1)).getComponent(
        0)).getComponent(0));
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    return modulesView.getBasePanel();
  }
}
