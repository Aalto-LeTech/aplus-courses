package fi.aalto.cs.intellij.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import fi.aalto.cs.intellij.ui.ModuleList;
import org.jetbrains.annotations.NotNull;

public class ModulesToolWindow implements ToolWindowFactory {

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    ModuleList moduleList = new ModuleList();
    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(moduleList.getBasePanel(), "", true);
    toolWindow.getContentManager().addContent(content);
  }

}
