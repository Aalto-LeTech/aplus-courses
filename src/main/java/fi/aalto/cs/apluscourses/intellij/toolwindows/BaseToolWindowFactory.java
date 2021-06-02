package fi.aalto.cs.apluscourses.intellij.toolwindows;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import fi.aalto.cs.apluscourses.intellij.actions.ActionGroups;
import java.util.List;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

public abstract class BaseToolWindowFactory implements ToolWindowFactory {
  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    JComponent component = createToolWindowContentInternal(project);
    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(component, "", true);
    toolWindow.getContentManager().addContent(content);
    ActionManager actionManager = ActionManager.getInstance();
    ActionGroup group = (ActionGroup) actionManager.getAction(ActionGroups.TOOL_WINDOW_ACTIONS);
    toolWindow.setTitleActions(List.of(group));
  }

  protected abstract JComponent createToolWindowContentInternal(@NotNull Project project);
}
