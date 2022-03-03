package fi.aalto.cs.apluscourses.intellij.actions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.ComponentInstaller;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import fi.aalto.cs.apluscourses.ui.InstallerDialogs;
import fi.aalto.cs.apluscourses.utils.BuildInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ListSelectionModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class InstallModuleActionTest {

  private Project project;
  private MainViewModel mainViewModel;
  private ComponentInstaller installer;
  private InstallerDialogs.Factory dialogsFactory;

  /**
   * Called before each test method call.  Initializes private fields.
   */
  @BeforeEach
  void createMockObjects() {
    project = mock(Project.class);

    mainViewModel = new MainViewModel(new Options());
    MainViewModelProvider mainViewModelProvider = mock(MainViewModelProvider.class);
    doReturn(mainViewModel).when(mainViewModelProvider).getMainViewModel(project);

    List<Module> modules = new ArrayList<>();
    modules.add(new ModelExtensions.TestModule("module1"));
    modules.add(new ModelExtensions.TestModule("module2"));
    modules.add(new ModelExtensions.TestModule("module3"));

    Course course = new ModelExtensions.TestCourse(
        "id",
        "course",
        "http://localhost:7766",
        Collections.emptyList(),
        modules,
        // libraries
        Collections.emptyList(),
        // exerciseModules
        Collections.emptyMap(),
        // resourceUrls
        Collections.emptyMap(),
        // vmOptions
        Collections.emptyMap(),
        // optionalCategories
        Collections.emptySet(),
        // autoInstallComponentNames
        Collections.emptyList(),
        // replInitialCommands
        Collections.emptyMap(),
        // courseVersion
        BuildInfo.INSTANCE.courseVersion,
        // tutorials
        Collections.emptyMap());
    mainViewModel.courseViewModel.set(new CourseViewModel(course));

    installer = mock(ComponentInstaller.class);

    dialogsFactory = mock(InstallerDialogs.Factory.class);
  }

  @SuppressWarnings({"ConstantConditions"})
  @Test
  void testUpdate() {
    InstallModuleAction action = new InstallModuleAction(p -> mainViewModel, (c, d) -> installer,
        dialogsFactory);

    Presentation presentation = new Presentation();
    AnActionEvent e = mock(AnActionEvent.class);
    doReturn(presentation).when(e).getPresentation();
    doReturn(project).when(e).getProject();

    action.update(e);
    Assertions.assertFalse(presentation.isEnabledAndVisible());

    ListSelectionModel selectionModel = mainViewModel.courseViewModel.get()
        .getModules()
        .getSelectionModel();

    selectionModel.addSelectionInterval(1, 2);
    action.update(e);
    Assertions.assertTrue(presentation.isEnabledAndVisible());

    selectionModel.clearSelection();
    action.update(e);
    Assertions.assertFalse(presentation.isEnabledAndVisible());
  }

  @SuppressWarnings({"unchecked", "ConstantConditions"})
  @Test
  void testActionPerformed() {
    InstallModuleAction action = new InstallModuleAction(p -> mainViewModel, (c, d) -> installer,
        dialogsFactory);

    AnActionEvent e = mock(AnActionEvent.class);

    ListSelectionModel selectionModel = mainViewModel.courseViewModel.get()
        .getModules()
        .getSelectionModel();

    selectionModel.addSelectionInterval(1, 2);

    action.actionPerformed(e);

    ArgumentCaptor<List<Component>> captor = ArgumentCaptor.forClass(List.class);

    verify(installer).installAsync(captor.capture(), any());

    List<Module> modules = mainViewModel.courseViewModel.get().getModel().getModules();

    Assertions.assertEquals(2, captor.getValue().size(), "installAsync() should be called with list of size 2.");
    Assertions.assertTrue(captor.getValue().contains(modules.get(1)), "The second module should get installed.");
    Assertions.assertTrue(captor.getValue().contains(modules.get(2)), "The third module should get installed.");
  }
}
