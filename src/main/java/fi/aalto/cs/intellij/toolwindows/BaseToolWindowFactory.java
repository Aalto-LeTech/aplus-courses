package fi.aalto.cs.intellij.toolwindows;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

public abstract class BaseToolWindowFactory implements ToolWindowFactory {
  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    JComponent component = createToolWindowContentInternal(project);
    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(component, "", true);
    toolWindow.getContentManager().addContent(content);
  }

  protected abstract JComponent createToolWindowContentInternal(@NotNull Project project);
}
