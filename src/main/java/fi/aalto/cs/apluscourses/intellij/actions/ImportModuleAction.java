package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.EdtTask;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.ComponentInstaller;
import fi.aalto.cs.apluscourses.model.ComponentInstallerImpl;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.base.BaseViewModel;
import fi.aalto.cs.apluscourses.utils.async.SimpleAsyncTaskManager;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ImportModuleAction extends DumbAwareAction {

  public static final String ACTION_ID = ImportModuleAction.class.getCanonicalName();
  @NotNull
  private final MainViewModelProvider mainViewModelProvider;
  @NotNull
  private final ComponentInstaller.Factory componentInstallerFactory;
  @NotNull
  private final DialogsFactory dialogsFactory;

  /**
   * Constructs an action using given main view model provider and module installer factory.
   * @param mainViewModelProvider A main view model provider.
   * @param componentInstallerFactory A component installer factory.
   */
  public ImportModuleAction(@NotNull MainViewModelProvider mainViewModelProvider,
                            @NotNull ComponentInstaller.Factory componentInstallerFactory,
                            @NotNull DialogsFactory dialogsFactory) {

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

  private static class InstallerDialogs implements ComponentInstaller.Dialogs {
    @Nullable
    private final Project project;

    public InstallerDialogs(@Nullable Project project) {
      this.project = project;
    }

    @Override
    public boolean shouldOverwrite(@NotNull Component component) {
      String name = component.getName();
      return new EdtTask<Integer>() {
        @Override
        protected Integer execute() {
          return Messages.showYesNoDialog(project, "CAUTION! Updating a module will overwrite your"
                  + " local changes in the module. Are you sure you want to update " + name  + "?",
              name, Messages.getWarningIcon());
        }
      }.executeAndWait() == Messages.YES;
    }
  }

  @FunctionalInterface
  public interface DialogsFactory {
    @NotNull
    public ComponentInstaller.Dialogs getDialogs(@Nullable Project project);
  }
}
