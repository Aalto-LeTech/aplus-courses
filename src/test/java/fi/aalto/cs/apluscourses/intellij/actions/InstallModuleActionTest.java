package fi.aalto.cs.apluscourses.intellij.actions;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
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
import org.junit.jupiter.api.BeforeEach;

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

    mainViewModel = new MainViewModel(new Options(), new Options());
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
        // autoInstallComponentNames
        Collections.emptyList(),
        // replInitialCommands
        Collections.emptyMap(),
        // courseVersion
        BuildInfo.INSTANCE.courseVersion,
        // tutorials
        Collections.emptyMap());
    mainViewModel.courseViewModel.set(new CourseViewModel(course, null));

    installer = mock(ComponentInstaller.class);

    dialogsFactory = mock(InstallerDialogs.Factory.class);
  }
}
