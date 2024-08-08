package fi.aalto.cs.apluscourses.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

public class OpenDocumentationAction extends DumbAwareAction {

//  @NotNull
//  private final MainViewModelProvider mainViewModelProvider;

//  @NotNull
//  private final UrlRenderer urlRenderer;

  /**
   * Action constructor.
   */
  public OpenDocumentationAction(
//      @NotNull UrlRenderer urlRenderer
  ) {
//    this.urlRenderer = urlRenderer;
  }

//  public OpenDocumentationAction() {
//    this(PluginSettings.getInstance(), new UrlRenderer());
//  }

  @Override
  public void update(@NotNull AnActionEvent e) {
//    CourseViewModel courseViewModel =
//        mainViewModelProvider.getMainViewModel(e.getProject()).courseViewModel.get();
//    e.getPresentation().setEnabled(courseViewModel != null && courseViewModel.getModules().canOpenDocumentation());
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
//    Project project = e.getProject();
//    if (project == null) {
//      return;
//    }
//    CourseViewModel courseViewModel =
//        mainViewModelProvider.getMainViewModel(project).courseViewModel.get();
//    if (courseViewModel == null) {
//      return;
//    }
//    var selection = courseViewModel.getModules().getSingleSelectedElement();
//    if (selection.isEmpty()) {
//      return;
//    }
//    var module = selection.get().getGet();
//    var virtualFile = LocalFileSystem.getInstance().findFileByIoFile(module.getDocumentationIndexFullPath().toFile());
//    if (virtualFile == null) {
//      return;
//    }
//    try {
//      urlRenderer.show(project, virtualFile);
//    } catch (Exception ex) {
//      Notifier.Companion.notify(new UrlRenderingErrorNotification(ex), project);
//    }
  }
}
