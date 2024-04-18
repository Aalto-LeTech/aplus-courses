package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.notifications.UrlRenderingErrorNotification;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.UrlRenderer;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import org.jetbrains.annotations.NotNull;

public class OpenDocumentationAction extends DumbAwareAction {

  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final UrlRenderer urlRenderer;

  @NotNull
  private final Notifier notifier;

  /**
   * Action constructor.
   */
  public OpenDocumentationAction(@NotNull MainViewModelProvider mainViewModelProvider,
                                 @NotNull UrlRenderer urlRenderer,
                                 @NotNull Notifier notifier) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.urlRenderer = urlRenderer;
    this.notifier = notifier;
  }

  public OpenDocumentationAction() {
    this(PluginSettings.getInstance(), new UrlRenderer(), new DefaultNotifier());
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    CourseViewModel courseViewModel =
        mainViewModelProvider.getMainViewModel(e.getProject()).courseViewModel.get();
    e.getPresentation().setEnabled(courseViewModel != null && courseViewModel.getModules().canOpenDocumentation());
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) {
      return;
    }
    CourseViewModel courseViewModel =
        mainViewModelProvider.getMainViewModel(project).courseViewModel.get();
    if (courseViewModel == null) {
      return;
    }
    var selection = courseViewModel.getModules().getSingleSelectedElement();
    if (selection.isEmpty()) {
      return;
    }
    var module = selection.get().getModel();
    var virtualFile = LocalFileSystem.getInstance().findFileByIoFile(module.getDocumentationIndexFullPath().toFile());
    if (virtualFile == null) {
      return;
    }
    try {
      urlRenderer.show(project, virtualFile);
    } catch (Exception ex) {
      notifier.notify(new UrlRenderingErrorNotification(ex), project);
    }
  }
}
