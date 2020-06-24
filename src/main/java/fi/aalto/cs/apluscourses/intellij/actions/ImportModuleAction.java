package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.ComponentInstaller;
import fi.aalto.cs.apluscourses.model.ComponentInstallerImpl;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.base.BaseViewModel;
import fi.aalto.cs.apluscourses.ui.InstallerDialogs;
import fi.aalto.cs.apluscourses.utils.async.SimpleAsyncTaskManager;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ImportModuleAction extends DumbAwareAction {

  public static final String ACTION_ID = ImportModuleAction.class.getCanonicalName();
  @NotNull
  private final MainViewModelProvider mainViewModelProvider;
  @NotNull
  private final ComponentInstaller.Factory componentInstallerFactory;
  @NotNull
  private final InstallerDialogs.Factory dialogsFactory;

  /**
   * Constructs an action using given main view model provider and module installer factory.
   * @param mainViewModelProvider A main view model provider.
   * @param componentInstallerFactory A component installer factory.
   */
  public ImportModuleAction(@NotNull MainViewModelProvider mainViewModelProvider,
                            @NotNull ComponentInstaller.Factory componentInstallerFactory,
                            @NotNull InstallerDialogs.Factory dialogsFactory) {

    this.mainViewModelProvider = mainViewModelProvider;
    this.componentInstallerFactory = componentInstallerFactory;
    this.dialogsFactory = dialogsFactory;
  }

  /**
   * Called by the framework.
   */
  public ImportModuleAction() {
    this(PluginSettings.getInstance(),
        new ComponentInstallerImpl.FactoryImpl<>(new SimpleAsyncTaskManager()),
        InstallerDialogs::new);
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
      List<Component> modules = courseViewModel.getModules().getSelectedElements()
          .stream()
          .map(BaseViewModel::getModel)
          .collect(Collectors.toList());
      Course course = courseViewModel.getModel();
      componentInstallerFactory.getInstallerFor(course, dialogsFactory.getDialogs(e.getProject()))
          .installAsync(modules, course::validate);
    }
  }

}
