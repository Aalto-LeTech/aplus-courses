package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.model.ModuleInstaller;
import fi.aalto.cs.apluscourses.model.ModuleInstallerImpl;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.base.BaseViewModel;
import fi.aalto.cs.apluscourses.utils.async.SimpleAsyncTaskManager;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ImportModuleAction extends AnAction {

  public static final String ACTION_ID =
      "fi.aalto.cs.apluscourses.intellij.actions.ImportModuleAction";
  @NotNull
  private final MainViewModelProvider mainViewModelProvider;
  @NotNull
  private final ModuleInstaller.Factory moduleInstallerFactory;

  public ImportModuleAction(@NotNull MainViewModelProvider mainViewModelProvider,
                            @NotNull ModuleInstaller.Factory moduleInstallerFactory) {

    this.mainViewModelProvider = mainViewModelProvider;
    this.moduleInstallerFactory = moduleInstallerFactory;
  }

  public ImportModuleAction() {
    this(PluginSettings.getInstance(),
        new ModuleInstallerImpl.Factory<>(new SimpleAsyncTaskManager()));
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    CourseViewModel courseViewModel =
        mainViewModelProvider.getMainViewModel(e.getProject()).courseViewModel.get();
    boolean isModuleSelected = courseViewModel != null
        && !courseViewModel.getModules().getSelectionModel().isSelectionEmpty();
    e.getPresentation().setEnabled(isModuleSelected);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    CourseViewModel courseViewModel =
        mainViewModelProvider.getMainViewModel(e.getProject()).courseViewModel.get();
    if (courseViewModel != null) {
      List<Module> modules = courseViewModel.getModules().getSelectedElements()
          .stream()
          .map(BaseViewModel::getModel)
          .collect(Collectors.toList());
      Course course = courseViewModel.getModel();
      moduleInstallerFactory.getInstallerFor(course).installAsync(modules);
    }
  }
}
