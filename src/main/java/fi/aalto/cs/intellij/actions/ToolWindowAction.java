package fi.aalto.cs.intellij.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import fi.aalto.cs.intellij.ui.ExerciseList;
import org.jetbrains.annotations.NotNull;

public class ToolWindowAction implements ToolWindowFactory {

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    ExerciseList exerciseList = new ExerciseList();
    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(exerciseList.getBasePanel(), "", true);
    toolWindow.getContentManager().addContent(content);
  }

}
