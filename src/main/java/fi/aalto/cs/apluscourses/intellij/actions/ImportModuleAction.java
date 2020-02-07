package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.model.ModuleInstaller;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.base.BaseViewModel;
import fi.aalto.cs.apluscourses.utils.SimpleAsyncTaskManager;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ImportModuleAction extends AnAction {

  public static final String ACTION_ID =
      "fi.aalto.cs.apluscourses.intellij.actions.ImportModuleAction";

  @Override
  public void update(@NotNull AnActionEvent e) {
    CourseViewModel course = PluginSettings.getInstance()
        .getMainViewModel(e.getProject()).courseViewModel.get();
    boolean isModuleSelected = course != null
        && !course.getModules().getSelectionModel().isSelectionEmpty();
    e.getPresentation().setEnabled(isModuleSelected);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    CourseViewModel course = PluginSettings.getInstance()
        .getMainViewModel(e.getProject()).courseViewModel.get();
    if (course != null) {
      List<Module> modules = course.getModules().getSelectedElements()
          .stream()
          .map(BaseViewModel::getModel)
          .collect(Collectors.toList());
      new ModuleInstaller<>(course.getModel(), new SimpleAsyncTaskManager())
          .installAsync(modules);
    }
  }
}
